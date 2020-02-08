import { BaseModel } from "./base-model";
import { Language } from "./language";
import { DayHourTimer } from "./dayHourTimer";

export class LanguageNotification extends BaseModel {
    language: Language;
    suggestions = true;
    newSongs = true;
    suggestionsDayHourTimer: DayHourTimer = new DayHourTimer();
    newSongsDayHourTimer: DayHourTimer = new DayHourTimer();
    private static minutes15 = 15 * 60 * 1000;
    suggestionsDelay = LanguageNotification.minutes15;
    newSongsDelay = LanguageNotification.minutes15;

    constructor(values: Object = {}) {
        super(values);
        Object.assign(this, values);
        this.suggestionsDayHourTimer = new DayHourTimer();
        this.suggestionsDayHourTimer.initialize(this.suggestionsDelay);
        this.newSongsDayHourTimer = new DayHourTimer();
        this.newSongsDayHourTimer.initialize(this.newSongsDelay);
    }
}