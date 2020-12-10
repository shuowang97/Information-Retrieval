import { Component, OnInit } from '@angular/core';
import { FormControl } from '@angular/forms';
import {debounceTime, filter, switchMap, tap} from 'rxjs/operators';
import {HomepageService} from '../homepage.service';


@Component({
  selector: 'app-homepage',
  templateUrl: './homepage.component.html',
  styleUrls: ['./homepage.component.css']
})
export class HomepageComponent implements OnInit {
  myRadio: string = '';
  keywords: string = '';
  docList: any = [];
  myControl = new FormControl();
  suggestionList: any = [];
  loadingSuggest = true;
  recommendWord:any = '';
  noResultFlag = false;

  constructor(private homepageService: HomepageService) { }

  ngOnInit(): void {
    this.myRadio = 'lucene';
    this.myControl.valueChanges.pipe(
      tap(() => this.noResultFlag = false),
      debounceTime(300),
      filter(query => {
        if(query.trim() === '') {
          this.loadingSuggest = true;
        }
        return query.trim() !== '';
      }),
      tap(() => this.loadingSuggest = true),
      tap(query => query.trim()),
      switchMap(query => this.homepageService.instantSearchAPI(query))
    ).subscribe(res => {
      this.loadingSuggest = false;
      this.suggestionList = res;
    })
  }

  solrSearch() {
    console.log(this.myRadio);
    console.log(this.myControl.value);

    if(this.myRadio === 'lucene') {
      this.homepageService.getSearchResLucene(this.myControl.value).subscribe(res => {
        // console.log(res);
        this.docList = res;
        console.log('doclist', this.docList);
        if(this.docList.length === 0) {
          this.homepageService.getRecommendWord(this.myControl.value).subscribe(res => {
            this.noResultFlag = true;
            this.recommendWord = res;
            console.log(this.recommendWord);
          })
        }else {
          this.noResultFlag = false;
        }
      })
    } else {
      this.homepageService.getSearchResWithPageRank(this.myControl.value).subscribe(res => {
        // console.log(res);
        this.docList = res;
      })
    }
  }

}
