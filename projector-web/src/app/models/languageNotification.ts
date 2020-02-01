import { BaseModel } from "./base-model";
import { Language } from "./language";

export class LanguageNotification extends BaseModel {
    language: Language;
    suggestions = false;
    newSongs = false;

    constructor(values: Object = {}) {
        super(values);
        Object.assign(this, values);
    }
}