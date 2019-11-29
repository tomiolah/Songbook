import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { Song, SongService, SongVerseDTO, SongVerseUI, SectionType } from '../../services/song-service.service';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Language } from "../../models/language";
import { LanguageDataService } from "../../services/language-data.service";
import { NewLanguageComponent } from "../new-language/new-language.component";
import { MatDialog, MatIconRegistry } from "@angular/material";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { replace } from "../new-song/new-song.component";
import { AuthenticateComponent } from "../authenticate/authenticate.component";
import { CdkDragDrop, moveItemInArray, copyArrayItem } from '@angular/cdk/drag-drop';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-edit-song',
  templateUrl: './edit-song.component.html',
  styleUrls: ['./edit-song.component.css']
})
export class EditSongComponent implements OnInit {
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
  languages = [];
  selectedLanguage;
  @Input()
  song: Song;
  editorType = 'verse';
  public youtubeUrl;
  public safeUrl: SafeResourceUrl = null;
  private songTextFormControl: FormControl;
  sectionTypes: {
    name: string;
    type: SectionType;
  }[];
  usedSectionTypes: {
    name: string;
    type: SectionType;
    text: string;
    verse: SongVerseUI;
    index: number;
  }[];
  sectionOrder: {
    name: string;
    type: SectionType;
    text: string;
    verse: SongVerseUI;
    index: number;
  }[];
  customSectionOrder = false;

  constructor(private fb: FormBuilder,
    private songService: SongService,
    private router: Router,
    private languageDataService: LanguageDataService,
    private dialog: MatDialog,
    iconRegistry: MatIconRegistry,
    public sanitizer: DomSanitizer,
    public auth: AuthService,
    private _changeDetectionRef: ChangeDetectorRef) {
    iconRegistry.addSvgIcon(
      'magic_tool',
      sanitizer.bypassSecurityTrustResourceUrl('assets/icons/magic_tool-icon.svg'));
    this.verses = [];
    this.languages = [];
    this.sectionTypes = [
      { name: 'Intro', type: SectionType.Intro },
      { name: 'Verse', type: SectionType.Verse },
      { name: 'Pre-Chorus', type: SectionType.Pre_chorus },
      { name: 'Chorus', type: SectionType.Chorus },
      { name: 'Bridge', type: SectionType.Bridge },
      { name: 'Coda', type: SectionType.Coda },
    ];
    this.usedSectionTypes = [];
    this.sectionOrder = [];
  }

  ngOnInit() {
    this.createForm();
    this.loadLanguage(false);
    this.calculateUsedSectionTypes();
  }

  ngAfterViewChecked(): void {
    this._changeDetectionRef.detectChanges();
  }

  noReturnPredicate() {
    return false;
  }

  removeSectionOrder(i: number) {
    this.sectionOrder.splice(i, 1);
  }

  drop(event: CdkDragDrop<string[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      copyArrayItem(event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex);
    }
    this.customSectionOrder = true;
  }

  changeMe(chip, verse: SongVerseUI) {
    let vm = this;
    let editSongComponent = this;
    setTimeout(function () {
      verse.type = chip.type;
      vm._changeDetectionRef.detectChanges();
      editSongComponent.calculateUsedSectionTypes();
    }, 10)
  }

  calculateUsedSectionTypes() {
    let i = 0;
    this.usedSectionTypes = [];
    for (const verse of this.verses) {
      let aChip = this.sectionTypes[0];
      for (const sectionType of this.sectionTypes) {
        if (sectionType.type == verse.type) {
          aChip = sectionType;
          break;
        }
      }
      const usedSection = {
        name: this.getSectionName(aChip, verse, i),
        type: verse.type,
        text: this.form.value['verse' + i],
        verse: verse,
        index: i
      };
      this.usedSectionTypes.push(usedSection);
      ++i;
    }
    this.calculateOrder();
  }

  setCustomSectionOrder(x: boolean) {
    this.customSectionOrder = x;
    this.calculateOrder();
  }

  calculateOrder() {
    if (this.customSectionOrder) {
      for (const sectionIndex of this.song.verseOrderList) {
        for (const usedSection of this.usedSectionTypes) {
          if (usedSection.index == sectionIndex) {
            this.sectionOrder.push(usedSection);
            break;
          }
        }
      }
    } else {
      this.sectionOrder = [];
      let chorus = null;
      let delta = 1;
      for (const usedSection of this.usedSectionTypes) {
        if (usedSection.type == SectionType.Chorus) {
          chorus = usedSection;
          delta = 0;
        } else {
          if (chorus != null && delta > 0) {
            this.sectionOrder.push(chorus);
          }
          ++delta;
        }
        this.sectionOrder.push(usedSection);
      }
      const type = this.sectionOrder[this.sectionOrder.length - 1].type;
      if (chorus != null && type != SectionType.Chorus && type != SectionType.Coda && delta > 0) {
        this.sectionOrder.push(chorus);
      }
    }
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
    this.youtubeUrl = "https://www.youtube.com/watch?v=" + this.song.youtubeUrl;
    if (this.youtubeUrl.endsWith("undefined")) {
      this.youtubeUrl = '';
    }
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
    this.addVerses();
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
    this.calculateUsedSectionTypes();
  }

  removeSection(sectionIndex: number) {
    const formValue = this.form.value;
    let i = 0;
    let song = new Song();
    song.songVerseDTOS = [];
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        if (sectionIndex != i) {
          const value = formValue[key];
          const songVerseDTO = new SongVerseDTO();
          songVerseDTO.text = value;
          songVerseDTO.chorus = this.verses[i].chorus;
          songVerseDTO.type = this.verses[i].type
          song.songVerseDTOS.push(songVerseDTO);
        }
        i = i + 1;
      }
    }
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        this.form.removeControl(key);
      }
    }
    this.verses.splice(0, this.verses.length);
    this.verseControls.splice(0, this.verseControls.length);
    i = 0;
    for (const verseI of song.songVerseDTOS) {
      const songVerse = new SongVerseUI();
      songVerse.type = verseI.type;
      let verse = verseI.text;
      const control = new FormControl(verse);
      control.setValue(verse);
      this.verseControls.push(control);
      this.form.addControl('verse' + i, control);
      control.patchValue(verse);
      this.verses.push(songVerse);
      ++i;
    }
    this.calculateUsedSectionTypes();
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
    this.song.verseOrder = null;
    this.song.author = formValue.author;
    this.song.songVerseDTOS = [];
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
    this.song.deleted = false;
    let url = formValue.youtubeUrl;
    this.song.youtubeUrl = null;
    if (url) {
      let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
      youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
      youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
      if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
        this.song.youtubeUrl = youtubeUrl;
      }
    }
    this.setVerseOrderListFromSectionOrder();
    this.updateSong();
  }

  private setVerseOrderListFromSectionOrder() {
    this.song.verseOrderList = [];
    for (const section of this.sectionOrder) {
      this.song.verseOrderList.push(section.index);
    }
  }

  openNewLanguageDialog(): void {
    const dialogRef = this.dialog.open(NewLanguageComponent);
    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.loadLanguage(true);
      }
    });
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
      this.calculateUsedSectionTypes();
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

  private updateSong() {
    const role = this.auth.getUser().getRolePath();
    this.songService.updateSong(role, this.song).subscribe(
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
      const songVerseUI = new SongVerseUI();
      songVerseUI.chorus = songVerse.chorus;
      songVerseUI.type = songVerse.type;
      this.verses.push(songVerseUI);
      this.verseControls.push(control);
      this.form.addControl('verse' + (this.verses.length - 1), control);
    }
    if (this.song.verseOrderList != null && this.song.verseOrderList.length > 0) {
      this.customSectionOrder = true;
    }
    this.calculateUsedSectionTypes();
    this.song.verseOrderList = [];
  }
}
