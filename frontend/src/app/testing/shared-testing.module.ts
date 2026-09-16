import { NgModule } from '@angular/core';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

@NgModule({
  exports: [
    HttpClientTestingModule,
    RouterTestingModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class SharedTestingModule {}
