import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedTestingModule } from '../../../testing/shared-testing.module';

import { OrderManagementComponent } from './order-management.component';

describe('OrderManagementComponent', () => {
  let component: OrderManagementComponent;
  let fixture: ComponentFixture<OrderManagementComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedTestingModule],
      declarations: [OrderManagementComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OrderManagementComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
