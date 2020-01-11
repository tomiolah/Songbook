import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { User } from '../models/user';
import { Observable } from 'rxjs/Observable';

@Injectable()
export class UserDataService {

  constructor(private api: ApiService) {
  }

  getUser(userId): Observable<User> {
    return this.api.getById(User, 'admin/api/user/', userId);
  }

  addUser(user: User, language: string): Observable<User> {
    return this.api.create(User, 'api/users?language=' + language, user);
  }

  activate(activationCode: string): Observable<any> {
    return this.api.post('user/api/user/activate?activationCode=' + activationCode);
  }

  getAll(): Observable<User[]> {
    return this.api.getAll(User, 'admin/api/users');
  }

  update(user: User) {
    user.id = user.uuid;
    return this.api.update(User, 'admin/api/user/', user);
  }
}
