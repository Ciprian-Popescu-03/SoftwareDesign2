import { ComponentFixture, TestBed } from '@angular/core/testing';

// 1. Point to the correct exported class name
import { ProductListComponent } from './product-list';

describe('ProductListComponent', () => {
  let component: ProductListComponent;
  let fixture: ComponentFixture<ProductListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ProductListComponent], // 2. Update here
    }).compileComponents();

    fixture = TestBed.createComponent(ProductListComponent); // 3. Update here
    component = fixture.componentInstance;
    fixture.detectChanges(); // Good practice to include this!
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
