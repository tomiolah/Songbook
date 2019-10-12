import {Injectable} from '@angular/core';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/do';
import 'rxjs/add/operator/delay';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import {Headers, Http, RequestOptions} from '@angular/http';
import {User} from '../models/user';

@Injectable()
export class AuthService {

  isLoggedIn = false;
  private FUser: User;

  // store the URL so we can redirect after logging in
  redirectUrl: string;

  constructor(private http: Http) {
  }

  login(username: string, password: string) {
    const params = new URLSearchParams();
    params.append('username', username);
    params.append('password', password);
    const headers = new Headers({'Content-Type': 'application/x-www-form-urlencoded'});
    const options = new RequestOptions({headers: headers});
    return this.http.post('/login', params.toString(), options)
      .map(res => res);
  }

  getUser(): User {
    return this.FUser;
  }

  setUser(user: User) {
    this.FUser = user;
    this.isLoggedIn = !!this.FUser;
  }

  logout(): void {
    this.http.get('/logout')
      .map(res => res).subscribe();
    this.isLoggedIn = false;
    localStorage.removeItem('currentUser');
  }

  getUserFromServer() {
    return this.http.get('/api/username')
      .map(res => res.json());
  }

  getUserFromLocalStorage() {
    this.setUser(new User(JSON.parse(localStorage.getItem('currentUser'))));
  }
}
