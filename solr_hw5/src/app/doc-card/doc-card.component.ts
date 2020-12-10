import {Component, Input, OnInit} from '@angular/core';

@Component({
  selector: 'app-doc-card',
  templateUrl: './doc-card.component.html',
  styleUrls: ['./doc-card.component.css']
})
export class DocCardComponent implements OnInit {

  @Input() doc: any;
  constructor() { }

  ngOnInit(): void {
  }

  goToUrl(): void {

  }

}
