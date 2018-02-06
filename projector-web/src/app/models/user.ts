import {BaseModel} from './base-model';

export class User extends BaseModel {

  email = '';
  phone = '';
  password = '';
  role = '';
  preferredLanguage = '';

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
