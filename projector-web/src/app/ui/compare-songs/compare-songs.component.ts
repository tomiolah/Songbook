import { Component, Input, OnChanges, SimpleChanges, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { ColorText, Song, LineCompare, LineWord, WordCompare } from "../../services/song-service.service";
import { SongListComponent } from '../song-list/song-list.component';

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
  @ViewChildren("leftLineCompares") leftLineComparesElements: QueryList<ElementRef>;
  @ViewChildren("rightLineCompares") rightLineComparesElements: QueryList<ElementRef>;
  @ViewChildren("leftWordCompares") leftWordComparesElements: QueryList<ElementRef>;
  @ViewChildren("rightWordCompares") rightWordComparesElements: QueryList<ElementRef>;
  @ViewChildren("leftCharacterCompares") leftCharacterComparesElements: QueryList<ElementRef>;
  @ViewChildren("rightCharacterCompares") rightCharacterComparesElements: QueryList<ElementRef>;
  @ViewChildren("leftDifferentLines") leftDifferentLines: QueryList<ElementRef>;
  @ViewChildren("rightDifferentLines") rightDifferentLines: QueryList<ElementRef>;

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

  static highestCommonLines(leftSongLines: string[], rightSongLines: string[]): string[] {
    let t = [];
    let i, j;
    for (i = 0; i < leftSongLines.length + 2; ++i) {
      t[i] = [];
      t[i][0] = 0;
    }
    for (j = 1; j < rightSongLines.length + 2; ++j) {
      t[0][j] = 0;
    }
    let c;
    for (i = 0; i < leftSongLines.length; ++i) {
      c = leftSongLines[i];
      for (j = 0; j < rightSongLines.length; ++j) {
        if (c == rightSongLines[j]) {
          t[i + 1][j + 1] = t[i][j] + 1;
        } else if (t[i + 1][j] > t[i][j + 1]) {
          t[i + 1][j + 1] = t[i + 1][j];
        } else {
          t[i + 1][j + 1] = t[i][j + 1];
        }
      }
    }
    i = leftSongLines.length;
    j = rightSongLines.length;
    let strings = [];
    while (i != 0 && j != 0) {
      if (t[i - 1][j] + 1 == t[i][j] && t[i][j] == t[i][j - 1] + 1) {
        strings.push(leftSongLines[i - 1]);
        --i;
        --j;
      } else {
        if (t[i][j - 1] > t[i - 1][j]) {
          --j;
        } else {
          --i;
        }
      }
    }
    return strings.reverse();
  }

  private static getTextFromReverseLetters(r: any[]) {
    return r;
  }

  private static createLines(song) {
    for (const songVerse of song.getVerses()) {
      songVerse.lines = [];
      for (const s of songVerse.text.split('\n')) {
        songVerse.lines.push(s);
      }
    }
  }

  private static getText(song: Song) {
    let text = '';
    for (let songVerse of song.getVerses()) {
      if (songVerse.lines != undefined) {
        for (let line of songVerse.lines) {
          text += line;
        }
      }
    }
    return text;
  }

  private static getLines(song: Song): string[] {
    let lines: string[] = [];
    for (const songVerse of song.getVerses()) {
      for (const line of songVerse.lines) {
        lines.push(line);
      }
    }
    return lines;
  }

  private static getWordsByLine(line: string): string[] {
    let words = [];
    let word = '';
    let otherThanWord = '';
    let c = '';
    for (let i = 0; i < line.length; ++i) {
      c = line.charAt(i);
      const stripCharacter = SongListComponent.stripAccents(c);
      if ('a' <= stripCharacter && stripCharacter <= 'z') {
        if (otherThanWord.length > 0) {
          words.push(otherThanWord);
          otherThanWord = '';
        }
        word = word + c;
      } else {
        otherThanWord = otherThanWord + c;
        if (word.length > 0) {
          words.push(word);
          word = '';
        }
      }
    }
    if (otherThanWord.length > 0) {
      words.push(otherThanWord);
    }
    if (word.length > 0) {
      words.push(word);
    }
    return words;
  }

  private static getCharactersByWords(words: string[]): string[] {
    let characters = [];
    for (const word of words) {
      for (const character of word) {
        characters.push(character);
      }
    }
    return characters;
  }

  private static getWordsByLines(lines: string[]): string[] {
    let words = [];
    for (const line of lines) {
      const wordsInLine = CompareSongsComponent.getWordsByLine(line);
      for (const word of wordsInLine) {
        words.push(word);
      }
    }
    return words;
  }

  private static createLineCompareLines(song: Song, otherSong: Song, commonStrings: string[]) {
    song.commonWordsCount = 0;
    song.commonCharacterCount = 0;
    otherSong.commonWordsCount = 0;
    otherSong.commonCharacterCount = 0;
    let lastLinesWithModifications: LineCompare[] = [];
    let otherLastLinesWithModifications: LineCompare[] = [];
    let otherI = 0;
    const otherVerses = otherSong.getVerses();
    if (otherVerses.length > 0) {
      otherVerses[0].lineCompareLines = [];
    }
    let lineOtherI = 0;
    let k = 0;
    for (const songVerse of song.getVerses()) {
      songVerse.lineCompareLines = [];
      for (const line of songVerse.lines) {
        let lineCompare = new LineCompare();
        lineCompare.text = line;
        if (k < commonStrings.length && line == commonStrings[k]) {
          lineCompare.color = true;
          lineCompare.commonCount = k;
          let found = false;
          while (otherI < otherSong.getVerses().length && !found) {
            const otherSongVerse = otherSong.getVerses()[otherI];
            while (lineOtherI < otherSongVerse.lines.length && !found) {
              const otherLine = otherSongVerse.lines[lineOtherI];
              let otherLineCompare = new LineCompare();
              otherLineCompare.text = otherLine;
              otherLineCompare.color = otherLine == commonStrings[k];
              if (otherLineCompare.color) {
                otherLineCompare.commonCount = k;
                CompareSongsComponent.calculateDifferencesByWordsForLines(lastLinesWithModifications, otherLastLinesWithModifications, song, otherSong);
                lastLinesWithModifications = [];
                otherLastLinesWithModifications = [];
                found = true;
              } else {
                otherLastLinesWithModifications.push(otherLineCompare);
              }
              otherSongVerse.lineCompareLines.push(otherLineCompare);
              ++lineOtherI;
            }
            if (lineOtherI >= otherSongVerse.lines.length) {
              ++otherI;
              const otherVerses = otherSong.getVerses();
              if (otherVerses.length > otherI) {
                otherVerses[otherI].lineCompareLines = [];
              }
              lineOtherI = 0;
            }
          }
          ++k;
        } else {
          lineCompare.color = false;
          lastLinesWithModifications.push(lineCompare);
        }
        songVerse.lineCompareLines.push(lineCompare);
      }
    }
    while (otherI < otherSong.getVerses().length) {
      const otherSongVerse = otherSong.getVerses()[otherI];
      while (lineOtherI < otherSongVerse.lines.length) {
        const otherLine = otherSongVerse.lines[lineOtherI];
        let otherLineCompare = new LineCompare();
        otherLineCompare.text = otherLine;
        otherLineCompare.color = false;
        otherLastLinesWithModifications.push(otherLineCompare);
        ++lineOtherI;
        otherSongVerse.lineCompareLines.push(otherLineCompare);
      }
      if (lineOtherI >= otherSongVerse.lines.length) {
        ++otherI;
        const otherVerses = otherSong.getVerses();
        if (otherVerses.length > otherI) {
          otherVerses[otherI].lineCompareLines = [];
        }
        lineOtherI = 0;
      }
    }
    CompareSongsComponent.calculateDifferencesByWordsForLines(lastLinesWithModifications, otherLastLinesWithModifications, song, otherSong);
  }

  private static createLineWordsForLineCompare(leftSongLineCompares: LineCompare[], rightSongLineCompares: LineCompare[], commonWords: string[], song: Song, otherSong: Song) {
    let lastWordsWithModifications: WordCompare[] = [];
    let otherLastWordsWithModifications: WordCompare[] = [];
    let otherI = 0;
    if (rightSongLineCompares.length > otherI) {
      const lineCompare = rightSongLineCompares[otherI];
      lineCompare.lineWord = new LineWord();
      lineCompare.lineWord.words = [];
    }
    let otherWordI = 0;
    let k = 0;
    for (const lineCompare of leftSongLineCompares) {
      let lineWord = new LineWord();
      lineWord.words = [];
      let modified = false;
      let words = CompareSongsComponent.getWordsByLine(lineCompare.text);
      for (const word of words) {
        let wordCompare = new WordCompare();
        wordCompare.text = word;
        if (word == commonWords[k]) {
          wordCompare.color = true;
          wordCompare.commonCount = song.commonWordsCount++;
          let found = false;
          while (otherI < rightSongLineCompares.length && !found) {
            const otherLineCompare = rightSongLineCompares[otherI];
            let otherWords = CompareSongsComponent.getWordsByLine(otherLineCompare.text);
            while (otherWordI < otherWords.length && !found) {
              const otherWord = otherWords[otherWordI];
              let otherWordCompare = new WordCompare();
              otherWordCompare.text = otherWord;
              otherWordCompare.color = otherWord == commonWords[k];
              if (otherWordCompare.color) {
                otherWordCompare.commonCount = otherSong.commonWordsCount++;
                CompareSongsComponent.calculateDifferencesByCharachtersForWords(lastWordsWithModifications, otherLastWordsWithModifications, song, otherSong);
                lastWordsWithModifications = [];
                otherLastWordsWithModifications = [];
                found = true;
              } else {
                otherLastWordsWithModifications.push(otherWordCompare);
              }
              otherLineCompare.lineWord.words.push(otherWordCompare);
              ++otherWordI;
            }
            if (otherWordI >= otherWords.length) {
              ++otherI;
              if (rightSongLineCompares.length > otherI) {
                const lineCompare = rightSongLineCompares[otherI];
                lineCompare.lineWord = new LineWord();
                lineCompare.lineWord.words = [];
              }
              otherWordI = 0;
            }
          }
          ++k;
        } else {
          wordCompare.color = false;
          lastWordsWithModifications.push(wordCompare);
          modified = true;
        }
        lineWord.words.push(wordCompare);
      }
      lineWord.modified = modified;
      lineCompare.lineWord = lineWord;
    }
    while (otherI < rightSongLineCompares.length) {
      const otherLineCompare = rightSongLineCompares[otherI];
      let otherWords = CompareSongsComponent.getWordsByLine(otherLineCompare.text);
      while (otherWordI < otherWords.length) {
        const otherWord = otherWords[otherWordI];
        let otherWordCompare = new WordCompare();
        otherWordCompare.text = otherWord;
        otherWordCompare.color = false;
        otherLastWordsWithModifications.push(otherWordCompare);
        otherLineCompare.lineWord.words.push(otherWordCompare);
        ++otherWordI;
      }
      if (otherWordI >= otherWords.length) {
        ++otherI;
        if (rightSongLineCompares.length > otherI) {
          const lineCompare = rightSongLineCompares[otherI];
          lineCompare.lineWord = new LineWord();
          lineCompare.lineWord.words = [];
        }
        otherWordI = 0;
      }
    }
    CompareSongsComponent.calculateDifferencesByCharachtersForWords(lastWordsWithModifications, otherLastWordsWithModifications, song, otherSong);
  }

  private static createCharactersForLineCompare(leftWords: WordCompare[], rightWords: WordCompare[], commonCharacters: string[], song: Song, otherSong: Song) {
    let otherI = 0;
    if (rightWords.length > otherI) {
      const wordCompare = rightWords[otherI];
      wordCompare.characters = [];
    }
    let otherCharacterI = 0;
    let k = 0;
    for (const wordCompare of leftWords) {
      wordCompare.characters = [];
      for (const character of wordCompare.text) {
        let colorCharacter = new ColorText();
        colorCharacter.text = character;
        colorCharacter.color = character == commonCharacters[k];
        if (colorCharacter.color) {
          colorCharacter.commonCount = song.commonCharacterCount++;
          let found = false;
          while (otherI < rightWords.length && !found) {
            const otherWordCompare = rightWords[otherI];
            let otherCharacters = otherWordCompare.text;
            while (otherCharacterI < otherCharacters.length && !found) {
              const otherCharacter = otherCharacters[otherCharacterI];
              let otherColorCharacter = new ColorText();
              otherColorCharacter.text = otherCharacter;
              otherColorCharacter.color = otherCharacter == commonCharacters[k];
              if (otherColorCharacter.color) {
                otherColorCharacter.commonCount = otherSong.commonCharacterCount++;
                found = true;
              }
              otherWordCompare.characters.push(otherColorCharacter);
              ++otherCharacterI;
            }
            if (otherCharacterI >= otherCharacters.length) {
              ++otherI;
              if (rightWords.length > otherI) {
                const wordCompare = rightWords[otherI];
                wordCompare.characters = [];
              }
              otherCharacterI = 0;
            }
          }
          ++k;
        }
        wordCompare.characters.push(colorCharacter);
      }
    }
    while (otherI < rightWords.length) {
      const otherWordCompare = rightWords[otherI];
      let otherCharacters = otherWordCompare.text;
      while (otherCharacterI < otherCharacters.length) {
        const otherCharacter = otherCharacters[otherCharacterI];
        let otherColorCharacter = new ColorText();
        otherColorCharacter.text = otherCharacter;
        otherColorCharacter.color = false;
        otherWordCompare.characters.push(otherColorCharacter);
        ++otherCharacterI;
      }
      if (otherCharacterI >= otherCharacters.length) {
        ++otherI;
        if (rightWords.length > otherI) {
          const wordCompare = rightWords[otherI];
          wordCompare.characters = [];
        }
        otherCharacterI = 0;
      }
    }
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

  private static getCommonWordsByCommonCharacter(leftSongWords: string[], rightSongWords: string[], commonCharacters: string[]): string[] {
    let commonWords = [];
    let k = 0;
    let otherI = 0;
    let otherCharacterI = 0;
    let otherK = 0;
    for (const word of leftSongWords) {
      let wordInCommon = true;
      for (const character of word) {
        if (character == commonCharacters[k]) {
          ++k;
        } else {
          wordInCommon = false;
        }
      }
      if (wordInCommon) {
        while (otherI < rightSongWords.length && otherK < k) {
          let otherWord = rightSongWords[otherI];
          wordInCommon = otherCharacterI == 0;
          while (otherCharacterI < otherWord.length && otherK < k) {
            const otherCharacter = otherWord[otherCharacterI];
            if (otherCharacter == commonCharacters[otherK]) {
              ++otherK;
            } else {
              wordInCommon = false;
            }
            ++otherCharacterI;
          }
          if (otherCharacterI >= otherWord.length) {
            ++otherI;
            otherCharacterI = 0;
          }
          wordInCommon = otherWord == word;
        }
        if (wordInCommon) {
          commonWords.push(word);
        }
      }
    }
    return commonWords;
  }

  private calculateDifferences(repeatChorus: boolean) {
    Object.assign(this.m_song, this.originalSong1);
    Object.assign(this.m_secondSong, this.originalSong2);
    this.m_song.repeatChorus = repeatChorus;
    this.m_secondSong.repeatChorus = repeatChorus;
    CompareSongsComponent.createLines(this.m_song);
    CompareSongsComponent.createLines(this.m_secondSong);
    this.calculateDifferencesByLines();
    this.getCommonStringsAndSetPercentage();
  }

  private getCommonStringsAndSetPercentage() {
    let a = CompareSongsComponent.getText(this.m_song);
    let b = CompareSongsComponent.getText(this.m_secondSong);
    let commonStrings = CompareSongsComponent.highestCommonStrings(a, b);
    let x = commonStrings.length;
    x = x / a.length;
    let y = commonStrings.length;
    y = y / b.length;
    this.percentage = (x + y) / 2;
    return commonStrings;
  }

  private calculateDifferencesByLines() {
    const leftSongLines = CompareSongsComponent.getLines(this.m_song);
    const rightSongLines = CompareSongsComponent.getLines(this.m_secondSong);
    let commonLines = CompareSongsComponent.highestCommonLines(leftSongLines, rightSongLines);
    let x = commonLines.length;
    x = x / leftSongLines.length;
    let y = commonLines.length;
    y = y / rightSongLines.length;
    this.percentage = (x + y) / 2;
    CompareSongsComponent.createLineCompareLines(this.m_song, this.m_secondSong, commonLines);
    setTimeout(() => {
      this.focusOnElement(this.leftDifferentLines.toArray()[0]);
      this.focusOnElement(this.rightDifferentLines.toArray()[0]);
    }, 1000);
  }

  private static calculateDifferencesByWordsForLines(leftSongLineCompares: LineCompare[], rightSongLineCompares: LineCompare[], song: Song, otherSong: Song) {
    let leftSongLines: string[] = [];
    for (const lineCompare of leftSongLineCompares) {
      leftSongLines.push(lineCompare.text);
    }
    let rightSongLines: string[] = [];
    for (const lineCompare of rightSongLineCompares) {
      rightSongLines.push(lineCompare.text);
    }
    let leftSongWords: string[] = CompareSongsComponent.getWordsByLines(leftSongLines);
    let rightSongWords: string[] = CompareSongsComponent.getWordsByLines(rightSongLines);
    let commonWords = CompareSongsComponent.highestCommonLines(leftSongWords, rightSongWords);
    let leftCharacters: string[] = CompareSongsComponent.getCharactersByWords(leftSongWords);
    let rightCharacters: string[] = CompareSongsComponent.getCharactersByWords(rightSongWords);
    let commonCharacters = CompareSongsComponent.highestCommonLines(leftCharacters, rightCharacters);
    let countCommonWordsCharacter = 0;
    for (const word of commonWords) {
      countCommonWordsCharacter = countCommonWordsCharacter + word.length;
    }
    if (commonCharacters.length * 100 / countCommonWordsCharacter > 110) {
      commonWords = CompareSongsComponent.getCommonWordsByCommonCharacter(leftSongWords, rightSongWords, commonCharacters);
    }
    CompareSongsComponent.createLineWordsForLineCompare(leftSongLineCompares, rightSongLineCompares, commonWords, song, otherSong);
  }

  private static calculateDifferencesByCharachtersForWords(leftWords: WordCompare[], rightWords: WordCompare[], song: Song, otherSong: Song) {
    let leftSongWords: string[] = [];
    for (const wordCompare of leftWords) {
      leftSongWords.push(wordCompare.text);
    }
    let rightSongWords: string[] = [];
    for (const wordCompare of rightWords) {
      rightSongWords.push(wordCompare.text);
    }
    let leftCharacters: string[] = CompareSongsComponent.getCharactersByWords(leftSongWords);
    let rightCharacters: string[] = CompareSongsComponent.getCharactersByWords(rightSongWords);
    let commonCharacters = CompareSongsComponent.highestCommonLines(leftCharacters, rightCharacters);
    CompareSongsComponent.createCharactersForLineCompare(leftWords, rightWords, commonCharacters, song, otherSong);
  }

  focusOnLeft(index: number) {
    const someElement = this.leftLineComparesElements.toArray()[index];
    if (someElement != undefined) {
      this.focusOnElement(someElement);
    }
  }

  focusOnRight(index: number) {
    const someElement = this.rightLineComparesElements.toArray()[index];
    if (someElement != undefined) {
      this.focusOnElement(someElement);
    }
  }

  focusOnLeftWord(index: number) {
    this.focusOnElement(this.leftWordComparesElements.toArray()[index]);
  }

  focusOnRightWord(index: number) {
    this.focusOnElement(this.rightWordComparesElements.toArray()[index]);
  }

  focusOnLeftCharacter(index: number) {
    this.focusOnElement(this.leftCharacterComparesElements.toArray()[index]);
  }

  focusOnRightCharacter(index: number) {
    this.focusOnElement(this.rightCharacterComparesElements.toArray()[index]);
  }

  focusOnElement(element: ElementRef) {
    if (element == undefined) {
      return;
    }
    element.nativeElement.scrollIntoViewIfNeeded(false);
    element.nativeElement.classList.remove("focusTo");
    element.nativeElement.classList.add("focusTo");
    setTimeout(() => element.nativeElement.classList.remove("focusTo"), 1000);
  }
}
