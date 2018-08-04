import {Component, Input, OnInit} from '@angular/core';
import {Song, SongService, SongVerseDTO} from '../../services/song-service.service';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {Language} from "../../models/language";
import {LanguageDataService} from "../../services/language-data.service";
import {NewLanguageComponent} from "../new-language/new-language.component";
import {MatDialog, MatIconRegistry} from "@angular/material";
import {DomSanitizer} from "@angular/platform-browser";
import {replace} from "../new-song/new-song.component";
import {AuthenticateComponent} from "../authenticate/authenticate.component";

@Component({
  selector: 'app-edit-song',
  templateUrl: './edit-song.component.html',
  styleUrls: ['./edit-song.component.css']
})
export class EditSongComponent implements OnInit {
  form: FormGroup;
  formErrors = {
    'title': ''
  };

  validationMessages = {
    'title': {
      'required': 'Required field',
    }
  };
  verses: SongVerseDTO[];
  verseControls: FormControl[];
  languages = [];
  selectedLanguage;
  @Input()
  song: Song;
  editorType = 'verse';
  private songTextFormControl: FormControl;

  constructor(private fb: FormBuilder,
              private songService: SongService,
              private router: Router,
              private languageDataService: LanguageDataService,
              private dialog: MatDialog,
              iconRegistry: MatIconRegistry, sanitizer: DomSanitizer) {
    iconRegistry.addSvgIcon(
      'magic_tool',
      sanitizer.bypassSecurityTrustResourceUrl('assets/icons/magic_tool-icon.svg'));
    this.verses = [];
    this.languages = [];
  }

  ngOnInit() {
    this.createForm();
    this.loadLanguage(false);
  }

  loadLanguage(selectLast: boolean) {
    this.languageDataService.getAll().subscribe(
      (languages) => {
        this.languages = languages;
        if (selectLast) {
          this.selectedLanguage = this.languages[this.languages.length - 1];
        } else {
          for (const language of this.languages) {
            if (language.uuid === this.song.languageDTO.uuid) {
              this.selectedLanguage = language;
              break;
            }
          }
        }
      }
    );
  }

  createForm() {
    let url = "https://www.youtube.com/watch?v=" + this.song.youtubeUrl;
    if (url.endsWith("undefined")) {
      url = '';
    }
    this.form = this.fb.group({
      'title': [this.song.title, [
        Validators.required,
      ]],
      'youtubeUrl': [url, [
        Validators.maxLength(52),
      ]],
    });
    this.verseControls = [];
    this.addVerses();
    this.songTextFormControl = new FormControl('');
    this.form.addControl('songText', this.songTextFormControl);
    this.form.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
  }

  addNewVerse() {
    const control = new FormControl('');
    this.verses.push(new SongVerseDTO());
    this.verseControls.push(control);
    this.form.addControl('verse' + (this.verses.length - 1), control);
  }

  onValueChanged() {
    if (!this.form) {
      return;
    }
    const form = this.form;

    for (const field in this.formErrors) {
      if (this.formErrors.hasOwnProperty(field)) {
        this.formErrors[field] = '';
        const control = form.get(field);

        if (control && control.dirty && !control.valid) {
          const messages = this.validationMessages[field];
          for (const key in control.errors) {
            if (control.errors.hasOwnProperty(key)) {
              this.formErrors[field] += messages[key];
              break;
            }
          }
        }
      }
    }
  }

  onSubmit() {
    const formValue = this.form.value;
    this.song.title = formValue.title;
    this.song.songVerseDTOS = [];
    this.song.languageDTO = this.selectedLanguage;
    let i = 0;
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse')) {
        const value = formValue[key];
        const songVerseDTO = new SongVerseDTO();
        songVerseDTO.text = value;
        songVerseDTO.chorus = this.verses[i].chorus;
        this.song.songVerseDTOS.push(songVerseDTO);
        i = i + 1;
      }
    }
    this.song.deleted = false;
    let url = formValue.youtubeUrl;
    this.song.youtubeUrl = null;
    if (url) {
      let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
      youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
      if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
        this.song.youtubeUrl = youtubeUrl;
      }
    }
    this.updateSong();
  }

  openNewLanguageDialog(): void {
    const dialogRef = this.dialog.open(NewLanguageComponent);
    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.loadLanguage(true);
      }
    });
  }

  // noinspection JSMethodCanBeStatic
  printLanguage(language: Language) {
    if (language.englishName === language.nativeName) {
      return language.englishName;
    }
    return language.englishName + " | " + language.nativeName;
  }

  setChorus(verseNumber) {
    this.verses[verseNumber].chorus = !this.verses[verseNumber].chorus;
  }

  isChorus(i) {
    if (this.verses[i].chorus) {
      return 'green';
    } else {
      return 'rgb(216, 205, 205)';
    }
  }

  editorTypeChange() {
    if (this.editorType === 'raw') {
      const formValue = this.form.value;
      let i = 0;
      let text = '';
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse')) {
          const value = formValue[key];
          if (text.length > 0) {
            text = text + "\n\n";
          }
          if (this.verses[i].chorus) {
            text = text + "[Chorus]\n";
          }
          text = text + value;
          i = i + 1;
        }
      }
      this.songTextFormControl.patchValue(text);
    } else {
      let i = 0;
      const formValue = this.form.value;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse')) {
          this.form.removeControl(key);
          ++i;
        }
      }
      this.verses.splice(0, this.verses.length);
      this.verseControls.splice(0, this.verseControls.length);
      i = 0;
      for (const verseI of this.songTextFormControl.value.split("\n\n")) {
        const songVerseDTO = new SongVerseDTO();
        songVerseDTO.chorus = false;
        const chorusString = "[Chorus]\n";
        let verse = verseI;
        if (verse.startsWith(chorusString)) {
          songVerseDTO.chorus = true;
          verse = verseI.substring(chorusString.length, verseI.length);
        }
        const control = new FormControl(verse);
        control.setValue(verse);
        this.verseControls.push(control);
        this.form.addControl('verse' + i, control);
        control.patchValue(verse);
        this.verses.push(songVerseDTO);
        ++i;
      }
    }
  }

  needToDisable() {
    return !this.form.valid || this.editorType === 'raw';
  }

  refactor() {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      let i = 0;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse')) {
          let newValue = replace(formValue, key);
          this.form.controls['verse' + i].setValue(newValue);
          this.form.controls['verse' + i].updateValueAndValidity();
          i = i + 1;
        }
      }
    }
  }

  private updateSong() {
    this.songService.updateSong(this.song).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        if (err.status === 405) {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
        }
      }
    );
  }

  private openAuthenticateDialog() {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.updateSong();
      }
    });
  }

  private addVerses() {
    for (const songVerse of this.song.songVerseDTOS) {
      const control = new FormControl('');
      control.setValue(songVerse.text);
      const songVerseDTO = new SongVerseDTO();
      songVerseDTO.chorus = songVerse.chorus;
      this.verses.push(songVerseDTO);
      this.verseControls.push(control);
      this.form.addControl('verse' + (this.verses.length - 1), control);
    }
  }
}
