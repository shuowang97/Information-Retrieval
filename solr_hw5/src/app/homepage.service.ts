import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class HomepageService {

  constructor(private http: HttpClient) { }

  getRecommendWord(keywords: any) {
    return this.http.get('/routes/recommend/' + keywords).pipe(
      map(res => {
        return res;
      })
    )
  }

  instantSearchAPI(query: any): any {
    return this.http.get('/routes/suggest/' + query).pipe(
      map(res => {
        // console.log('homepage', res);
        return res;
      })
    )
  }

  getSearchResLucene(keywords: string) {
    return this.http.get('/routes/search/' + keywords).pipe(
      map(res => {
        return res;
      })
    )
  }

  getSearchResWithPageRank(keywords: string) {
   return this.http.get('/routes/rank/' + keywords).pipe(
     map(res => {
       return res;
     })
   )
  }
}
