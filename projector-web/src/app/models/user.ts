import { BaseModel } from './base-model';
import { Role } from './role';

export class User extends BaseModel {

  email = '';
  password = '';
  role = Role.ROLE_USER;
  preferredLanguage = '';
  surname = '';
  firstName = '';
  activated = false;
  modifiedDate: number;
  createdDate: number;

  nr: number;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }

  getActivatedString() {
    return this.activated ? '' : 'Not activated';
  }

  getRoleString() {
    return Role.getAsString(this.role);
  }

  isAdmin(): Boolean {
    return this.role == Role.ROLE_ADMIN;
  }

  isUser(): Boolean {
    return this.role == Role.ROLE_USER;
  }

  isReviewer(): Boolean {
    return this.role == Role.ROLE_REVIEWER;
  }
}
