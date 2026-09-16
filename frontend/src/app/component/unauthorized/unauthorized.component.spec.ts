import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedTestingModule } from '../../testing/shared-testing.module';

import { UnauthorizedComponent } from './unauthorized.component';

describe('UnauthorizedComponent', () => {
  let component: UnauthorizedComponent;
  let fixture: ComponentFixture<UnauthorizedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedTestingModule],
      declarations: [UnauthorizedComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UnauthorizedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
