import {BaseModel} from "./base-model";

export class Language extends BaseModel {
  englishName: '';
  nativeName: '';

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}
