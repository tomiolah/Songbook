import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  CanActivateChild,
  CanLoad,
  Route,
  Router,
  RouterStateSnapshot
} from '@angular/router';
import { AuthService } from './auth.service';

@Injectable()
export class AuthGuard implements CanActivate, CanActivateChild, CanLoad {
  constructor(private authService: AuthService, private router: Router) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    this.getUserFromLocalStorage();
    const url: string = state.url;
    this.authService.redirectUrl = url;
    return this.checkLogin(url);
  }

  canActivateChild(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    this.getUserFromLocalStorage();
    this.authService.redirectUrl = state.url;
    return this.canActivate(route, state);
  }

  canLoad(route: Route): boolean {
    this.getUserFromLocalStorage();
    const url = `/${route.path}`;
    return this.checkLogin(url);
  }

  checkLogin(url: string): boolean {
    if (this.authService.isLoggedIn) {
      if (url.startsWith('/admin')) {
        if (this.authService.user != null && this.authService.user.role === 'ROLE_ADMIN') {
          return true;
        }
      } else if (url.startsWith('/user')) {
        if (this.authService.user != null && (this.authService.user.role === 'ROLE_USER' || this.authService.user.role === 'ROLE_ADMIN')) {
          return true;
        }
      } else {
        return true;
      }
    }
    // Store the attempted URL for redirecting
    this.authService.redirectUrl = url;
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/login']);
    return false;
  }

  getUserFromLocalStorage() {
    this.authService.user = JSON.parse(localStorage.getItem('currentUser'));
    this.authService.isLoggedIn = !!this.authService.user;
  }
}
