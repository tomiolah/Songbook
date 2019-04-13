import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { User } from '../models/user';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class UserDataService {

  constructor(private api: ApiService) {
  }

  addUser(user: User, language: string): Observable<User> {
    return this.api.create(User, 'api/users?language=' + language, user);
  }

  activate(activationCode: string): Observable<any> {
    return this.api.post('user/api/user/activate?activationCode=' + activationCode);
  }
}
