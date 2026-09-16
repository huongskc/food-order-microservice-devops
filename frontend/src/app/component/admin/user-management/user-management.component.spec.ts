import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedTestingModule } from '../../../testing/shared-testing.module';

import { UserManagementComponent } from './user-management.component';

describe('UserManagementComponent', () => {
  let component: UserManagementComponent;
  let fixture: ComponentFixture<UserManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedTestingModule],
      declarations: [UserManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
