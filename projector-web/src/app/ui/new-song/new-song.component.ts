import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Song, SongService, SongVerseDTO} from '../../services/song-service.service';
import {Router} from '@angular/router';
import {Language} from "../../models/language";
import {LanguageDataService} from "../../services/language-data.service";
import {MatDialog} from "@angular/material";
import {NewLanguageComponent} from "../new-language/new-language.component";

@Component({
  selector: 'app-new-song',
  templateUrl: './new-song.component.html',
  styleUrls: ['./new-song.component.css']
})
export class NewSongComponent implements OnInit {
  form: FormGroup;
  formErrors = {
    'title': ''
  };

  validationMessages = {
    'title': {
      'required': 'Required field',
    }
  };
  verses: string[];
  verseControls: FormControl[];
  languages: Language[];
  selectedLanguage;
  private song: Song;

  constructor(private fb: FormBuilder,
              private songService: SongService,
              private router: Router,
              private languageDataService: LanguageDataService,
              private dialog: MatDialog) {
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
        }
      }
    );
  }

  createForm() {
    this.song = new Song();
    this.form = this.fb.group({
      'title': [this.song.title, [
        Validators.required,
      ]],
    });
    this.verseControls = [];
    this.addNewVerse();
    this.form.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
  }

  addNewVerse() {
    const control = new FormControl('');
    this.verses.push('');
    this.verseControls.push(control);
    this.form.addControl('verse' + (this.verses.length - 1), control);
  }

  removeVerse(index) {
    if (index > -1) {
      let i = 0;
      const formValue = this.form.value;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key !== 'title') {
          this.verses[i] = formValue[key];
          this.verseControls.splice(i, 1);
          this.form.removeControl('verse' + i);
          ++i;
        }
      }
      this.verses.splice(index, 1);
      i = 0;
      for (const verse of this.verses) {
        const control = new FormControl(verse);
        this.verseControls.push(control);
        this.form.addControl('verse' + i++, control);
      }
    }
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
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key !== 'title') {
        const value = formValue[key];
        const songVerseDTO = new SongVerseDTO();
        songVerseDTO.text = value;
        this.song.songVerseDTOS.push(songVerseDTO);
      }
    }
    this.songService.createSong(this.song).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        console.log(err);
      }
    );
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
}
