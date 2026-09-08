import { TestBed } from '@angular/core/testing';
import { CanActivateFn, Router } from '@angular/router';

import { AuthGuard } from './auth.guard';
import { AuthService } from '../service/auth.service';

describe('AuthGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => AuthGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        { provide: Router, useValue: { createUrlTree: jasmine.createSpy('createUrlTree') } },
        { provide: AuthService, useValue: { isLoggedIn: jasmine.createSpy('isLoggedIn').and.returnValue(true), getRoleFromToken: jasmine.createSpy('getRoleFromToken').and.returnValue('USER') } }
      ]
    });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});

