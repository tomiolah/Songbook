import {BaseModel} from "./base-model";
import {Song} from "../services/song-service.service";

export class Language extends BaseModel {
  englishName: '';
  nativeName: '';
  songTitles: Song[];

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }

  printLanguage() {
    if (this.englishName === this.nativeName) {
      return this.englishName;
    }
    return this.englishName + " | " + this.nativeName;
  }
}
