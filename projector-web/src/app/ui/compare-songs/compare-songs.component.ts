import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {ColorLine, ColorText, Song, SongVerseDTO} from "../../services/song-service.service";

@Component({
  selector: 'app-compare-songs',
  templateUrl: './compare-songs.component.html',
  styleUrls: ['./compare-songs.component.css']
})
export class CompareSongsComponent implements OnChanges {

  m_song: Song;
  m_secondSong: Song;
  originalSong1: Song;
  originalSong2: Song;
  repeatChorus: boolean;
  percentage = 0;

  constructor() {
    const text = localStorage.getItem("repeatChorus");
    if (text === undefined || text == null || text.trim().length == 0) {
      this.repeatChorus = false;
    } else {
      this.repeatChorus = JSON.parse(text);
    }
  }

  @Input()
  set song(song: Song) {
    this.m_song = new Song(song);
    this.originalSong1 = new Song(song);
  }

  @Input()
  set secondSong(secondSong: Song) {
    this.m_secondSong = new Song(secondSong);
    this.originalSong2 = new Song(secondSong);
  }

  public static highestCommonStrings(a: string, b: string) {
    let t = [];
    let i, j;
    for (i = 0; i < a.length + 2; ++i) {
      t[i] = [];
      t[i][0] = 0;
    }
    for (j = 1; j < b.length + 2; ++j) {
      t[0][j] = 0;
    }
    let c;
    for (i = 0; i < a.length; ++i) {
      c = a.charAt(i);
      for (j = 0; j < b.length; ++j) {
        if (c == b.charAt(j)) {
          t[i + 1][j + 1] = t[i][j] + 1;
        } else if (t[i + 1][j] > t[i][j + 1]) {
          t[i + 1][j + 1] = t[i + 1][j];
        } else {
          t[i + 1][j + 1] = t[i][j + 1];
        }
      }
    }
    let r = [];
    i = a.length;
    j = b.length;
    let strings = [];
    while (i != 0 && j != 0) {
      if (t[i - 1][j] + 1 == t[i][j] && t[i][j] == t[i][j - 1] + 1) {
        r.push(a.charAt(i - 1));
        --i;
        --j;
      } else {
        if (r.length > 0) {
          for (let letter of CompareSongsComponent.getTextFromReverseLetters(r)) {
            strings.push(letter);
          }
          r = [];
        }
        if (t[i][j - 1] > t[i - 1][j]) {
          --j;
        } else {
          --i;
        }
      }
    }
    if (r.length > 0) {
      for (let letter of CompareSongsComponent.getTextFromReverseLetters(r)) {
        strings.push(letter);
      }
    }
    return strings.reverse();
  }

  private static getTextFromReverseLetters(r: any[]) {
    return r;
  }

  private static createLines(song) {
    for (const songVerse of song.songVerseDTOS) {
      songVerse.lines = [];
      for (const s of songVerse.text.split('\n')) {
        songVerse.lines.push(s);
      }
    }
  }

  private static createColorLines(song, commonStrings: string[]) {
    let k = 0;
    let tmpText = '';
    let color = true;
    for (const songVerse of song.songVerseDTOS) {
      songVerse.colorLines = [];
      for (const line of songVerse.lines) {
        let colorLine = new ColorLine();
        colorLine.texts = [];
        let c = '';
        for (let i = 0; i < line.length; ++i) {
          c = line.charAt(i);
          if (c == commonStrings[k]) {
            if (!color) {
              let colorText = new ColorText();
              colorText.text = tmpText;
              colorText.color = color;
              colorLine.texts.push(colorText);
              tmpText = c;
              color = true;
            }
            else {
              tmpText += c;
            }
            ++k;
          } else {
            if (color) {
              let colorText = new ColorText();
              colorText.text = tmpText;
              colorText.color = color;
              colorLine.texts.push(colorText);
              tmpText = c;
              color = false;
            }
            else {
              tmpText += c;
            }
          }
        }
        if (tmpText.length > 0) {
          let colorText = new ColorText();
          colorText.text = tmpText;
          colorText.color = color;
          colorLine.texts.push(colorText);
        }
        tmpText = '';
        songVerse.colorLines.push(colorLine);
      }
    }
  }

  private static getText(song: Song, repeatChorus: boolean) {
    let verseList: SongVerseDTO[];
    verseList = [];
    let verses = song.songVerseDTOS;
    let chorus = new SongVerseDTO();
    let size = verses.length;
    for (let i = 0; i < size; ++i) {
      let songVerse = verses[i];
      verseList.push(songVerse);
      if (repeatChorus) {
        if (songVerse.chorus) {
          Object.assign(chorus, songVerse);
        } else if (chorus.chorus !== null && chorus.chorus) {
          if (i + 1 < size) {
            if (!verses[i + 1].chorus) {
              verseList.push(chorus);
            }
          } else {
            verseList.push(chorus);
          }
        }
      }
    }
    song.songVerseDTOS = verseList;
    let text = '';
    for (let songVerse of verseList) {
      for (let line of songVerse.lines) {
        text += line;
      }
    }
    return text;
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.calculateDifferences(this.repeatChorus);
  }

// noinspection JSMethodCanBeStatic
  getColor(colorText: ColorText) {
    return colorText.color ? '#000000' : '#ff0c00';
  }

  changeRepeatChorus() {
    this.repeatChorus = !this.repeatChorus;
    this.calculateDifferences(this.repeatChorus);
    localStorage.setItem("repeatChorus", JSON.stringify(this.repeatChorus));
  }

  private calculateDifferences(repeatChorus: boolean) {
    Object.assign(this.m_song, this.originalSong1);
    Object.assign(this.m_secondSong, this.originalSong2);
    CompareSongsComponent.createLines(this.m_song);
    CompareSongsComponent.createLines(this.m_secondSong);
    let a = CompareSongsComponent.getText(this.m_song, repeatChorus);
    let b = CompareSongsComponent.getText(this.m_secondSong, repeatChorus);
    let commonStrings = CompareSongsComponent.highestCommonStrings(a, b);
    let x = commonStrings.length;
    x = x / a.length;
    let y = commonStrings.length;
    y = y / b.length;
    this.percentage = (x + y) / 2;
    CompareSongsComponent.createColorLines(this.m_song, commonStrings);
    CompareSongsComponent.createColorLines(this.m_secondSong, commonStrings);
  }
}
