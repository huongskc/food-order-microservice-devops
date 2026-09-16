import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SharedTestingModule } from '../../testing/shared-testing.module';

import { OrderConfirmationComponent } from './order-confirmation.component';

describe('OrderConfirmationComponent', () => {
  let component: OrderConfirmationComponent;
  let fixture: ComponentFixture<OrderConfirmationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SharedTestingModule],
      declarations: [OrderConfirmationComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(OrderConfirmationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
