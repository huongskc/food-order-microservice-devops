import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedTestingModule } from '../../../testing/shared-testing.module';

import { UserFormDialogComponent } from './user-form-dialog.component';

describe('UserFormDialogComponent', () => {
  let component: UserFormDialogComponent;
  let fixture: ComponentFixture<UserFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedTestingModule],
      declarations: [UserFormDialogComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
