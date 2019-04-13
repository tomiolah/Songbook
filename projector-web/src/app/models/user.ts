import {BaseModel} from './base-model';

export class User extends BaseModel {

  email = '';
  password = '';
  role = '';
  preferredLanguage = '';
  sureName = '';
  firstName = '';

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
