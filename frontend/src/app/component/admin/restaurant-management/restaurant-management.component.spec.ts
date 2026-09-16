import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedTestingModule } from '../../../testing/shared-testing.module';

import { RestaurantManagementComponent } from './restaurant-management.component';

describe('RestaurantManagementComponent', () => {
  let component: RestaurantManagementComponent;
  let fixture: ComponentFixture<RestaurantManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedTestingModule],
      declarations: [RestaurantManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(RestaurantManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
