/* Connects the supplied interface to the workshop API. */
let csrfToken = null;
let csrfHeader = 'X-CSRF-TOKEN';
let mutationInProgress = false;
let modalPreviousFocus = null;

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, ch => ({'&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;'}[ch]));
}

function workshopNow() {
  const parts = new Intl.DateTimeFormat('en-CA', {timeZone:'Asia/Ho_Chi_Minh', year:'numeric', month:'2-digit', day:'2-digit',
    hour:'2-digit', minute:'2-digit', hourCycle:'h23'}).formatToParts(new Date());
  return Object.fromEntries(parts.filter(part => part.type !== 'literal').map(part => [part.type, part.value]));
}
getFormattedDateOffset = function (offset = 0) {
  const now = workshopNow();
  return new Date(Date.UTC(Number(now.year), Number(now.month) - 1, Number(now.day) + offset)).toISOString().slice(0, 10);
};
function bookableSlot(date, slot) {
  if (!date || !slot || new Date(date + 'T00:00:00Z').getUTCDay() === 0) return false;
  const today = getFormattedDateOffset();
  if (date < today) return false;
  const now = workshopNow();
  return date > today || slot > `${now.hour}:${now.minute}`;
}
for (let offset = 0; offset < 8; offset++) {
  const date = getFormattedDateOffset(offset);
  const slot = TIME_SLOTS.find(time => bookableSlot(date, time));
  if (slot) { selectedBookingDate = date; selectedBookingSlot = slot; break; }
}

async function refreshCsrf() {
  const response = await fetch('/api/csrf', {credentials: 'same-origin'});
  if (!response.ok) throw new Error('Không thể kết nối đến máy chủ');
  const data = await response.json();
  csrfToken = data.token; csrfHeader = data.headerName;
}

async function api(path, {method = 'GET', body, form = false} = {}) {
  const headers = {};
  if (method !== 'GET') {
    if (!csrfToken) await refreshCsrf();
    headers[csrfHeader] = csrfToken;
    headers['Content-Type'] = form ? 'application/x-www-form-urlencoded;charset=UTF-8' : 'application/json';
  }
  const response = await fetch(path, {method, headers, credentials:'same-origin',
    body: body === undefined ? undefined : form ? new URLSearchParams(body) : JSON.stringify(body)});
  if (!response.ok) {
    let message = response.status === 401 ? 'Số điện thoại hoặc mật khẩu không đúng, hoặc phiên đăng nhập đã hết hạn'
      : response.status === 403 ? 'Bạn không có quyền thực hiện thao tác này' : 'Không thể thực hiện yêu cầu';
    try { message = (await response.json()).message || message; } catch (_) { /* Security responses may have no JSON body. */ }
    throw new Error(message);
  }
  if (response.status === 204 || response.headers.get('content-length') === '0') return null;
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

async function refreshState() {
  appState = await api('/api/state');
  syncAllViews();
}

async function perform(action) {
  if (mutationInProgress) return;
  mutationInProgress = true;
  try { await action(); }
  catch (error) { showToast(error.message || 'Không thể kết nối đến máy chủ', 'error'); }
  finally { mutationInProgress = false; }
}

async function initializeApp() {
  try {
    await refreshCsrf();
    await refreshState();
    const account = appState.accounts.find(a => a.id === appState.activeAccountId);
    if (appState.authenticated && appState.currentRole === 'CUSTOMER') {
      document.getElementById('bookCustomerName').value = account.name;
      document.getElementById('bookCustomerPhone').value = account.phone;
    }
    document.getElementById('bookDateInput').min = getFormattedDateOffset(0);
    window.setInterval(() => {
      if (!document.hidden && !mutationInProgress) refreshState().catch(() => {});
    }, 30000);
  } catch (error) { showToast(error.message || 'Không thể tải dữ liệu', 'error'); }
}

const originalHeader = renderHeaderAndNav;
renderHeaderAndNav = function () {
  originalHeader();
  document.getElementById('dropdownAccountList').innerHTML = '';
  document.getElementById('accountAction').textContent = appState.authenticated ? 'Đăng xuất' : 'Đăng nhập / Đăng ký';
};

const originalOpenModal = openModal;
const originalCloseModal = closeModal;
openModal = function (id) {
  const modal = document.getElementById(id);
  modalPreviousFocus = document.activeElement;
  modal.setAttribute('role', 'dialog'); modal.setAttribute('aria-modal', 'true');
  const title = modal.querySelector('h3');
  if (title) { if (!title.id) title.id = `${id}-title`; modal.setAttribute('aria-labelledby', title.id); }
  originalOpenModal(id);
  modal.querySelector('button, input, select, textarea')?.focus();
};
closeModal = function (id) {
  originalCloseModal(id);
  if (modalPreviousFocus && document.contains(modalPreviousFocus)) modalPreviousFocus.focus();
};

// The API exposes aggregate occupancy, not other customers' appointment details.
getSlotLoadCount = function (date, slot, excludeId = null) {
  let count = appState.slotCounts[`${date}|${slot}`] || 0;
  const excluded = appState.appointments.find(a => a.id === excludeId);
  if (excluded && excluded.date === date && excluded.slot === slot && STATUS_META[excluded.status]?.countsCapacity) count--;
  return Math.max(0, count);
};

const originalBookingSlots = renderBookingTimeSlots;
renderBookingTimeSlots = function () {
  if (!bookableSlot(selectedBookingDate, selectedBookingSlot) ||
      getSlotLoadCount(selectedBookingDate, selectedBookingSlot) >= appState.maxCapacityPerSlot) {
    selectedBookingSlot = TIME_SLOTS.find(slot => bookableSlot(selectedBookingDate, slot) &&
      getSlotLoadCount(selectedBookingDate, slot) < appState.maxCapacityPerSlot) || null;
  }
  originalBookingSlots();
  document.querySelectorAll('#bookingTimeSlotGrid button').forEach((button, index) => {
    if (!bookableSlot(selectedBookingDate, TIME_SLOTS[index])) {
      button.disabled = true; button.classList.add('opacity-50', 'cursor-not-allowed');
      button.lastElementChild.textContent = new Date(selectedBookingDate + 'T00:00:00Z').getUTCDay() === 0 ? 'Xưởng nghỉ Chủ nhật' : 'Đã qua';
    }
  });
};
const originalRescheduleSlots = renderRescheduleSlots;
renderRescheduleSlots = function () {
  const date = document.getElementById('rescheduleDateInput').value;
  if (!bookableSlot(date, rescheduleSelectedSlot)) rescheduleSelectedSlot = null;
  originalRescheduleSlots();
  document.querySelectorAll('#rescheduleSlotGrid button').forEach((button, index) => {
    if (!bookableSlot(date, TIME_SLOTS[index])) {
      button.disabled = true; button.classList.add('opacity-40', 'cursor-not-allowed');
      button.lastElementChild.textContent = 'Không nhận';
    }
  });
};
const originalWalkInSlots = populateWalkinSlots;
populateWalkinSlots = function () {
  originalWalkInSlots();
  const date = document.getElementById('walkinDate').value;
  document.querySelectorAll('#walkinSlot option').forEach(option => {
    if (!bookableSlot(date, option.value)) { option.disabled = true; option.textContent += ' — Không nhận'; }
  });
};

// Build category controls using DOM APIs so category names are never executable code.
renderServiceCategoryPills = function () {
  const container = document.getElementById('serviceCategoryPills');
  container.replaceChildren();
  const categories = ['Tất cả', ...new Set(appState.services.filter(s => s.active).map(s => s.category))];
  for (const category of categories) {
    const button = document.createElement('button');
    button.type = 'button'; button.textContent = category;
    button.className = 'px-3 py-1 rounded-lg text-xs font-semibold whitespace-nowrap transition ' +
      (selectedCategoryFilter === category ? 'bg-signal text-white' : 'card-elevated text-[var(--text-muted)]');
    button.addEventListener('click', () => setServiceCategory(category));
    container.appendChild(button);
  }
};

setCustomerTrackScope = function () { customerTrackScope = 'MINE'; renderCustomerTrackingList(); };
openLoginModal = function () {
  document.getElementById('userDropdownMenu').classList.add('hidden');
  toggleRegistration(false); openModal('modalLogin');
  document.getElementById('loginPhoneInput').focus();
};
function toggleRegistration(show) {
  document.getElementById('loginFields').classList.toggle('hidden', show);
  document.getElementById('registrationFields').classList.toggle('hidden', !show);
  document.getElementById('loginTitle').textContent = show ? 'Tạo tài khoản khách hàng' : 'Đăng nhập Hệ thống';
}
async function login(phone, password) {
  await api('/api/login', {method:'POST', form:true, body:{phone, password}});
  csrfToken = null; await refreshCsrf(); await refreshState();
  closeModal('modalLogin');
  const account = appState.accounts.find(a => a.id === appState.activeAccountId);
  if (account.role === 'CUSTOMER') {
    document.getElementById('bookCustomerName').value = account.name;
    document.getElementById('bookCustomerPhone').value = account.phone;
  }
  document.getElementById('loginPasswordInput').value = '';
  document.getElementById('registerPassword').value = '';
  showToast('Đăng nhập thành công', 'success');
}
handlePhoneLogin = function () {
  perform(() => login(document.getElementById('loginPhoneInput').value.trim(), document.getElementById('loginPasswordInput').value));
};
function registerCustomer() {
  perform(async () => {
    const name = document.getElementById('registerName').value.trim();
    const phone = document.getElementById('registerPhone').value.trim();
    const password = document.getElementById('registerPassword').value;
    await api('/api/register', {method:'POST', body:{name, phone, password, meta:''}});
    await login(phone, password);
  });
}
function handleAccountAction() {
  if (!appState.authenticated) { openLoginModal(); return; }
  perform(async () => {
    await api('/api/logout', {method:'POST'}); csrfToken = null; await refreshCsrf();
    document.getElementById('bookCustomerName').value = '';
    document.getElementById('bookCustomerPhone').value = '';
    document.getElementById('bookLicensePlate').value = '';
    document.getElementById('bookCarModel').value = '';
    document.getElementById('userDropdownMenu').classList.add('hidden');
    customerActiveTab = 'BOOKING'; await refreshState(); switchCustomerTab('BOOKING');
    showToast('Đã đăng xuất', 'info');
  });
}

submitCustomerBooking = function () {
  if (!appState.authenticated) { openLoginModal(); showToast('Vui lòng đăng nhập để đặt lịch', 'info'); return; }
  perform(async () => {
    const body = {
      customerName: document.getElementById('bookCustomerName').value.trim(),
      phone: document.getElementById('bookCustomerPhone').value.trim(),
      licensePlate: document.getElementById('bookLicensePlate').value.trim().toUpperCase(),
      carModel: document.getElementById('bookCarModel').value.trim(),
      date:selectedBookingDate, slot:selectedBookingSlot, serviceIds:selectedBookingServiceIds,
      note:document.getElementById('bookSymptomNote').value.trim(), walkIn:false
    };
    const created = await api('/api/appointments', {method:'POST', body});
    document.getElementById('bookSymptomNote').value = '';
    await refreshState(); switchCustomerTab('TRACKING'); showToast(`Đã đặt lịch #${created.id}`, 'success');
  });
};

confirmRescheduleBooking = function () {
  perform(async () => {
    const id = document.getElementById('rescheduleApptId').value;
    const date = document.getElementById('rescheduleDateInput').value;
    const serviceIds = Array.from(document.querySelectorAll('input[name="rescheduleSrv"]:checked')).map(item => item.value);
    await api(`/api/appointments/${encodeURIComponent(id)}/schedule`, {method:'PUT', body:{date, slot:rescheduleSelectedSlot, serviceIds}});
    closeModal('modalReschedule'); await refreshState(); showToast('Đã đổi lịch và gửi xác nhận lại', 'success');
  });
};

confirmCancelOrReject = function () {
  perform(async () => {
    const id = document.getElementById('cancelApptId').value;
    const status = document.getElementById('cancelActionMode').value === 'CUSTOMER_CANCEL' ? 'CANCELLED' : 'REJECTED';
    const reason = document.getElementById('cancelReasonInput').value.trim();
    await api(`/api/appointments/${encodeURIComponent(id)}/status`, {method:'PUT', body:{status, reason}});
    closeModal('modalCancelOrReject'); await refreshState(); showToast('Đã cập nhật lịch hẹn', 'success');
  });
};

submitStaffConfirm = function () {
  perform(async () => {
    const id = document.getElementById('staffConfirmApptId').value;
    const body = {status:'CONFIRMED', bay:document.getElementById('staffConfirmBay').value,
      technician:document.getElementById('staffConfirmTech').value, reason:document.getElementById('staffConfirmNote').value.trim()};
    await api(`/api/appointments/${encodeURIComponent(id)}/status`, {method:'PUT', body});
    closeModal('modalStaffConfirm'); await refreshState(); showToast('Đã duyệt lịch hẹn', 'success');
  });
};
updateAppointmentStatus = function (id, status) {
  perform(async () => {
    await api(`/api/appointments/${encodeURIComponent(id)}/status`, {method:'PUT', body:{status}});
    await refreshState(); showToast('Đã cập nhật trạng thái lịch hẹn', 'success');
  });
};
const originalOpenWalkIn = openWalkInModal;
openWalkInModal = function () {
  originalOpenWalkIn();
  document.getElementById('walkinBay').innerHTML = document.getElementById('staffConfirmBay').innerHTML;
  document.getElementById('walkinTech').innerHTML = document.getElementById('staffConfirmTech').innerHTML;
};
submitWalkInBooking = function () {
  perform(async () => {
    const body = {customerName:document.getElementById('walkinName').value.trim(), phone:document.getElementById('walkinPhone').value.trim(),
      licensePlate:document.getElementById('walkinPlate').value.trim().toUpperCase(), carModel:document.getElementById('walkinModel').value.trim(),
      date:document.getElementById('walkinDate').value, slot:document.getElementById('walkinSlot').value,
      serviceIds:Array.from(document.querySelectorAll('input[name="walkinSrv"]:checked')).map(item => item.value),
      walkIn:true, note:'Tiếp nhận trực tiếp', bay:document.getElementById('walkinBay').value, technician:document.getElementById('walkinTech').value};
    const created = await api('/api/appointments', {method:'POST', body});
    closeModal('modalWalkIn'); await refreshState(); showToast(`Đã tạo lịch #${created.id}`, 'success');
  });
};

toggleServiceActive = function (id) {
  perform(async () => {
    const item = appState.services.find(s => s.id === id);
    const {id: ignoredId, ...fields} = item;
    await api(`/api/services/${encodeURIComponent(id)}`, {method:'PUT', body:{...fields, active:!item.active}});
    await refreshState(); showToast('Đã cập nhật trạng thái dịch vụ', 'success');
  });
};
const originalServiceEditor = openServiceEditorModal;
openServiceEditorModal = function (id = null) {
  originalServiceEditor(id); document.getElementById('editSrvCode').readOnly = !!id;
};
saveServiceFromModal = function () {
  perform(async () => {
    const id = document.getElementById('editSrvId').value;
    const existing = appState.services.find(s => s.id === id);
    const checklist = document.getElementById('editSrvChecklist').value.split(';').map(s => s.trim()).filter(Boolean);
    const body = {code:document.getElementById('editSrvCode').value.trim().toUpperCase(),
      category:document.getElementById('editSrvCategory').value, name:document.getElementById('editSrvName').value.trim(),
      duration:Number(document.getElementById('editSrvDuration').value), price:Number(document.getElementById('editSrvPrice').value),
      checklist, description:checklist.join(' • '), warranty:existing?.warranty || '', active:existing?.active ?? true};
    await api(id ? `/api/services/${encodeURIComponent(id)}` : '/api/services', {method:id ? 'PUT' : 'POST', body});
    closeModal('modalServiceEditor'); await refreshState(); showToast('Đã lưu dịch vụ', 'success');
  });
};
adjustMaxCapacity = function (delta) {
  perform(async () => {
    await api('/api/capacity', {method:'PUT', body:{capacity:appState.maxCapacityPerSlot + delta}});
    await refreshState(); showToast('Đã cập nhật công suất', 'success');
  });
};
const originalOpenAddUser = openAddUserModal;
openAddUserModal = function () { originalOpenAddUser(); document.getElementById('newUserPassword').value = ''; };
saveNewAccount = function () {
  perform(async () => {
    const body = {name:document.getElementById('newUserName').value.trim(), phone:document.getElementById('newUserPhone').value.trim(),
      role:document.getElementById('newUserRole').value, meta:document.getElementById('newUserMeta').value.trim(),
      password:document.getElementById('newUserPassword').value};
    await api('/api/accounts', {method:'POST', body});
    document.getElementById('newUserPassword').value = ''; closeModal('modalAddUser');
    await refreshState(); showToast('Đã tạo tài khoản', 'success');
  });
};

document.addEventListener('keydown', event => {
  const modal = document.querySelector('[id^="modal"][role="dialog"]:not(.hidden)');
  if (event.key === 'Escape' && modal) { event.preventDefault(); closeModal(modal.id); }
  if (event.key === 'Tab' && modal) {
    const focusable = [...modal.querySelectorAll('button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])')]
      .filter(element => element.getClientRects().length);
    if (focusable.length && event.shiftKey && document.activeElement === focusable[0]) {
      event.preventDefault(); focusable[focusable.length - 1].focus();
    } else if (focusable.length && !event.shiftKey && document.activeElement === focusable[focusable.length - 1]) {
      event.preventDefault(); focusable[0].focus();
    }
  }
  if (event.key === 'Enter' && !document.getElementById('modalLogin').classList.contains('hidden')) {
    event.preventDefault();
    if (document.getElementById('registrationFields').classList.contains('hidden')) handlePhoneLogin(); else registerCustomer();
  }
});
