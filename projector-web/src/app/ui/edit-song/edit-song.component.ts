import {Component, Input, OnInit} from '@angular/core';
import {Song, SongService, SongVerseDTO} from '../../services/song-service.service';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Router} from '@angular/router';
import {Language} from "../../models/language";
import {LanguageDataService} from "../../services/language-data.service";
import {NewLanguageComponent} from "../new-language/new-language.component";
import {MatDialog} from "@angular/material";

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
  verses: string[];
  verseControls: FormControl[];
  languages = [];
  selectedLanguage;
  @Input()
  song: Song;

  constructor(private fb: FormBuilder,
              private songService: SongService,
              private router: Router,
              private languageDataService: LanguageDataService,
              private dialog: MatDialog) {
    this.verses = [];
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
    this.form = this.fb.group({
      'title': [this.song.title, [
        Validators.required,
      ]],
    });
    this.verseControls = [];
    this.addVerses();
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
      let verses: string[];
      verses = [];
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key !== 'title') {
          verses[i] = formValue[key];
          this.verseControls.splice(i, 1);
          this.form.removeControl('verse' + i);
          ++i;
        }
      }
      verses.splice(index, 1);
      this.verses.splice(index, 1);
      i = 0;
      for (const verse of verses) {
        const control = new FormControl(verse);
        control.setValue(verse);
        this.verseControls.push(control);
        this.form.addControl('verse' + i, control);
        ++i;
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
    this.song.deleted = false;
    this.songService.updateSong(this.song).subscribe(
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

  private addVerses() {
    for (const songVerse of this.song.songVerseDTOS) {
      const control = new FormControl('');
      control.setValue(songVerse.text);
      this.verses.push('');
      this.verseControls.push(control);
      this.form.addControl('verse' + (this.verses.length - 1), control);
    }
  }
}
