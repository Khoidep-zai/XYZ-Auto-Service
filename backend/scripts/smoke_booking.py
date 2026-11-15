#!/usr/bin/env python3
"""Optional smoke test for a RUNNING XYZ Auto Service demo server.

Run on an isolated test database: this script CREATES one appointment and CONFIRMS it.
Environment: BASE_URL, CUSTOMER_PHONE, CUSTOMER_PASSWORD, STAFF_PHONE, STAFF_PASSWORD.
No third-party Python dependencies; Python >=3.9 required.
"""
import os
import sys
import json
from urllib.request import Request, build_opener, HTTPCookieProcessor
from urllib.parse import urlencode
from urllib.error import HTTPError
from http.cookiejar import CookieJar
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

BASE = os.environ.get('BASE_URL', 'http://localhost:8080').rstrip('/')
CUSTOMER_PHONE = os.environ.get('CUSTOMER_PHONE', '0988888888')
CUSTOMER_PASSWORD = os.environ.get('CUSTOMER_PASSWORD', '12345678')
STAFF_PHONE = os.environ.get('STAFF_PHONE', '0912345678')
STAFF_PASSWORD = os.environ.get('STAFF_PASSWORD', '12345678')

class Session:
    def __init__(self):
        self.opener = build_opener(HTTPCookieProcessor(CookieJar()))
        self.token = None
        self.csrf_header = 'X-CSRF-TOKEN'

    def request(self, method, path, data=None, form=False):
        headers = {}
        if method not in ('GET', 'HEAD'):
            if not self.token:
                self.refresh_csrf()
            headers[self.csrf_header] = self.token
            headers['Content-Type'] = 'application/x-www-form-urlencoded' if form else 'application/json'
        payload = None if data is None else (urlencode(data).encode() if form else json.dumps(data).encode())
        req = Request(BASE + path, data=payload, method=method, headers=headers)
        try:
            with self.opener.open(req, timeout=12) as response:
                content = response.read().decode('utf8')
                return response.status, json.loads(content) if content else None
        except HTTPError as ex:
            raw = ex.read().decode('utf8', 'replace')
            raise RuntimeError(f'{method} {path} failed HTTP {ex.code}: {raw[:350]}') from ex

    def refresh_csrf(self):
        with self.opener.open(BASE + '/api/csrf', timeout=12) as response:
            data = json.load(response)
        self.token = data['token']
        self.csrf_header = data['headerName']

    def login(self, phone, password):
        result, _ = self.request('POST', '/api/login', {'phone': phone, 'password': password}, form=True)
        assert result == 204, result
        self.refresh_csrf()


def main():
    print('XYZ Auto Service smoke test –', BASE)
    customer = Session(); customer.refresh_csrf(); customer.login(CUSTOMER_PHONE, CUSTOMER_PASSWORD)
    result, state = customer.request('GET', '/api/state')
    assert result == 200 and state.get('authenticated') and state.get('currentRole') == 'CUSTOMER'
    service = next((s for s in state['services'] if s.get('active')), None)
    assert service is not None, 'No active services'
    date = datetime.now(ZoneInfo('Asia/Ho_Chi_Minh')).date() + timedelta(days=1)
    while date.weekday() == 6:
        date += timedelta(days=1)
    key = date.isoformat()
    capacity = state['maxCapacityPerSlot']
    slot = next((time for time in ('08:00','09:00','10:00','11:00','13:00','14:00','15:00','16:00')
                 if state.get('slotCounts',{}).get(key+'|'+time,0) < capacity), None)
    if not slot:
        raise RuntimeError('No slots available for smoke test; select a different date in a disposable DB')
    plate = '59A-%05d' % (int(datetime.now().timestamp())%100000)
    account = next(a for a in state['accounts'] if a['id'] == state['activeAccountId'])
    booking = {'customerName': account['name'], 'phone': CUSTOMER_PHONE, 'licensePlate':plate,
               'carModel':'Toyota Vios (Smoke Test)', 'date':key, 'slot':slot,
               'serviceIds':[service['id']], 'note':'API smoke test – database demo only', 'walkIn':False}
    status, saved = customer.request('POST', '/api/appointments', booking)
    assert status == 201 and saved['id']
    appointment_id = str(saved['id'])
    _, state = customer.request('GET', '/api/state')
    item = next(a for a in state['appointments'] if a['id']==appointment_id)
    assert item['status'] == 'PENDING' and item['licensePlate'] == plate
    print('[OK] Online booking ID:',appointment_id, 'slot', key,slot)

    staff=Session();staff.refresh_csrf();staff.login(STAFF_PHONE, STAFF_PASSWORD)
    _,staff_state=staff.request('GET','/api/state')
    assert staff_state['currentRole'] in ('STAFF','MANAGER')
    status,_=staff.request('PUT',f'/api/appointments/{appointment_id}/status',
            {'status':'CONFIRMED','bay':'Khoang 04','technician':'KTV Demo'})
    assert status==200
    _,customer_state=customer.request('GET','/api/state')
    item=next(a for a in customer_state['appointments'] if a['id']==appointment_id)
    assert item['status']=='CONFIRMED'
    print('[OK] STAFF confirmed appointment ID:',appointment_id)
    print('[OK] Smoke test passed against:',BASE)
    print('Verify DB manually: SELECT id,status,license_plate FROM workshop_appointments WHERE id =',appointment_id,';')

if __name__=='__main__':
    try: main()
    except Exception as exc:
        print('[FAIL]',exc,file=sys.stderr);sys.exit(1)
