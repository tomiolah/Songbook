import {ChangeDetectionStrategy, Component, OnInit, ChangeDetectorRef} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {Song, SongService, SongVerseDTO, SongVerseUI, SectionType} from '../../services/song-service.service';
import {Router} from '@angular/router';
import {Language} from "../../models/language";
import {LanguageDataService} from "../../services/language-data.service";
import {MatDialog, MatIconRegistry, MatSnackBar} from "@angular/material";
import {NewLanguageComponent} from "../new-language/new-language.component";
import {DomSanitizer, SafeResourceUrl, Title} from "@angular/platform-browser";
import { AuthenticateComponent } from '../authenticate/authenticate.component';

export function replace(formValue: any, key) {
  const value = formValue[key];
  let newValue: string = value.trim();
  newValue = replaceMatch(newValue, /([ \t])([.?!,':])/g, '$2');
  newValue = replaceMatch(newValue, /([.?!,:])([^ “".?!,:)])/g, '$1 $2');
  newValue = replaceMatch(newValue, /: \//g, ' :/');
  newValue = replaceMatch(newValue, /\/ :/g, '/: ');
  newValue = replaceMatch(newValue, / /g, ' ');
  newValue = replaceMatch(newValue, / {2}/g, ' ');
  newValue = replaceMatch(newValue, /\. \. \./g, '…');
  newValue = replaceMatch(newValue, /\.\.\./g, '…');
  newValue = replaceMatch(newValue, /\.([^ "])/g, '. $1');
  newValue = replaceMatch(newValue, / \)/g, ')');
  newValue = replaceMatch(newValue, /\( /g, '(');
  newValue = replaceMatch(newValue, /\. "/g, '."');
  newValue = replaceMatch(newValue, /! "/g, '!"');
  newValue = replaceMatch(newValue, /\r\n/g, '\n');
  newValue = replaceMatch(newValue, /\n\n/g, '\n');
  newValue = replaceMatch(newValue, / \t/g, ' ');
  newValue = replaceMatch(newValue, /\t /g, ' ');
  newValue = replaceMatch(newValue, / \n/g, '\n');
  newValue = replaceMatch(newValue, /\n /g, '\n');
  newValue = replaceMatch(newValue, /\t\n/g, '\n');
  newValue = replaceMatch(newValue, /Ş/g, 'Ș');
  newValue = replaceMatch(newValue, /ş/g, 'ș');
  newValue = replaceMatch(newValue, /Ţ/g, 'Ț');
  newValue = replaceMatch(newValue, /ţ/g, 'ț');
  newValue = replaceMatch(newValue, /ã/g, 'ă');
  newValue = replaceMatch(newValue, /õ/g, 'ő');
  newValue = replaceMatch(newValue, /Õ/g, 'Ő');
  newValue = replaceMatch(newValue, /û/g, 'ű');
  return newValue;
}

function replaceMatch(newValue: string, matcher, replaceValue) {
  while (newValue.match(matcher)) {
    newValue = newValue.replace(matcher, replaceValue);
  }
  return newValue;
}

@Component({
  selector: 'app-new-song',
  templateUrl: './new-song.component.html',
  styleUrls: ['../edit-song/edit-song.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NewSongComponent implements OnInit {
  form: FormGroup;
  formErrors = {
    'title': '',
    'verseOrder': ''
  };

  validationMessages = {
    'title': {
      'required': 'Required field',
    },
    'verseOrder': {}
  };
  verses: SongVerseUI[];
  verseControls: FormControl[];
  languages: Language[];
  selectedLanguage;
  editorType = 'verse';
  song: Song;
  showSimilarities = false;
  similar: Song[];
  secondSong: Song;
  receivedSimilar = false;
  public youtubeUrl = '';
  public safeUrl: SafeResourceUrl = null;
  private songTextFormControl: FormControl;
  sectionTypes: {
    name: string;
    type: SectionType;
  }[];

  constructor(private fb: FormBuilder,
              private songService: SongService,
              private router: Router,
              private languageDataService: LanguageDataService,
              private titleService: Title,
              private dialog: MatDialog,
              iconRegistry: MatIconRegistry,
              private snackBar: MatSnackBar,
              public sanitizer: DomSanitizer,
              private _changeDetectionRef : ChangeDetectorRef) {
    iconRegistry.addSvgIcon(
      'magic_tool',
      sanitizer.bypassSecurityTrustResourceUrl('assets/icons/magic_tool-icon.svg'));
    this.verses = [];
    this.languages = [];
    this.sectionTypes = [
      { name: 'Intro', type: SectionType.Intro},
      { name: 'Verse', type: SectionType.Verse},
      { name: 'Pre-Chorus', type: SectionType.Pre_chorus},
      { name: 'Chorus', type: SectionType.Chorus},
      { name: 'Bridge', type: SectionType.Bridge},
      { name: 'Coda', type: SectionType.Coda},
    ];
  }

  ngOnInit() {
    this.titleService.setTitle('New song');
    this.createForm();
    this.loadLanguage(false);
  }

  ngAfterViewChecked(): void {
    this._changeDetectionRef.detectChanges();
  }

  changeMe(chip, verse: SongVerseUI) {
    let vm = this;
    setTimeout(function () {
      verse.type = chip.type;
      vm._changeDetectionRef.detectChanges();
    }, 10);
  }

  getSectionName(chip, verse: SongVerseUI, k: number) {
    if (chip.type == verse.type) {
      let count = 1;
      for (let i = 0; i < k; ++i) {
        if (this.verses[i].type == chip.type) {
          ++count;
        }
      }
      let allCount = 0;
      for (const aVerse of this.verses) {
        if (aVerse.type == verse.type) {
          ++allCount;
        }
      }
      if (allCount > 1) {
        return chip.name + ' ' + count;
      }
    }
    return chip.name;
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
      'youtubeUrl': [this.youtubeUrl, [
        Validators.maxLength(52),
      ]],
      'verseOrder': [this.song.verseOrder, []],
      'author': [this.song.author, []],
    });
    this.verseControls = [];
    this.addNewVerse();
    this.songTextFormControl = new FormControl('');
    this.form.addControl('songText', this.songTextFormControl);
    this.form.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
  }

  addNewVerse() {
    const control = new FormControl('');
    let section = new SongVerseUI();
    section.type = SectionType.Verse;
    this.verses.push(section);
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
    this.song.verseOrder = formValue.verseOrder;
    this.song.author = formValue.author;
    this.song.languageDTO = this.selectedLanguage;
    let i = 0;
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        const value = formValue[key];
        const songVerseDTO = new SongVerseDTO();
        songVerseDTO.text = value;
        songVerseDTO.chorus = this.verses[i].chorus;
        songVerseDTO.type = this.verses[i].type
        this.song.songVerseDTOS.push(songVerseDTO);
        i = i + 1;
      }
    }
    this.similar = [];
    this.songService.getSimilarByPost(this.song).subscribe((songs) => {
      this.similar = songs;
      if (songs.length > 0) {
        this.secondSong = this.similar[0];
      } else {
        this.insertNewSong();
      }
      this.receivedSimilar = true;
    });
    this.showSimilarities = true;
  }

  insertNewSong() {
    let url = this.form.value.youtubeUrl;
    this.song.youtubeUrl = null;
    if (url) {
      let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
      youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
      youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
      if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
        this.song.youtubeUrl = youtubeUrl;
      }
    }
    this.songService.createSong(this.song).subscribe(
      (song) => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/song/' + song.uuid]);
      },
      (err) => {
        if (err.message === 'Unexpected token < in JSON at position 0') {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
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
        this.insertNewSong();
      }
    });
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

  editorTypeChange() {
    if (this.editorType === 'raw') {
      const formValue = this.form.value;
      let i = 0;
      let text = '';
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          const value = formValue[key];
          if (text.length > 0) {
            text = text + "\n\n";
          }
          const type = this.verses[i].type;
          if (type != SectionType.Verse) {
            for (const sectionType of this.sectionTypes) {
              if (type == sectionType.type) {
                text = text + "[" + sectionType.name + "]\n";
                break;
              }
            }
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
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          this.form.removeControl(key);
          ++i;
        }
      }
      this.verses.splice(0, this.verses.length);
      this.verseControls.splice(0, this.verseControls.length);
      i = 0;
      for (const verseI of this.songTextFormControl.value.split("\n\n")) {
        const songVerse = new SongVerseUI();
        songVerse.type = SectionType.Verse;
        let verse = verseI;
        for (const sectionType of this.sectionTypes) {
          const sectionString = "[" + sectionType.name + "]\n";
          if (verse.startsWith(sectionString)) {
            songVerse.type = sectionType.type;
            verse = verseI.substring(sectionString.length, verseI.length);
          }
        }
        const control = new FormControl(verse);
        control.setValue(verse);
        this.verseControls.push(control);
        this.form.addControl('verse' + i, control);
        control.patchValue(verse);
        this.verses.push(songVerse);
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
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          let newValue = replace(formValue, key);
          this.form.controls['verse' + i].setValue(newValue);
          this.form.controls['verse' + i].updateValueAndValidity();
          i = i + 1;
        }
      }
    }
  }

  selectSecondSong(song: Song) {
    this.secondSong = song;
  }

  calculateUrlId() {
    let youtubeUrl = this.form.value.youtubeUrl.replace("https://www.youtube.com/watch?v=", "");
    youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
    youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
    let indexOf = youtubeUrl.indexOf('?');
    if (indexOf >= 0) {
      youtubeUrl = youtubeUrl.substring(0, indexOf);
    }
    if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl("https://www.youtube.com/embed/" + youtubeUrl);
    } else {
      this.safeUrl = null;
    }
  }
}
