import {Injectable} from '@angular/core';
import {HttpClientBasicAuth} from '../HttpClient/httpClient';
import {Observable} from 'rxjs/Observable';
import 'rxjs/Rx';
import * as globals from '../globals';
import {LoginService} from '../login/login.service';

@Injectable()
export class FacilitiesService {

  constructor(private http: HttpClientBasicAuth, private loginService: LoginService) {
    console.log('Facilities service is running');
  }

  getFacilities(page: number): Observable<any> {
    return this.http.get(globals.FACILITIES_BASEURL + '?page=' + String(page) + '&size=10');
  }

}
