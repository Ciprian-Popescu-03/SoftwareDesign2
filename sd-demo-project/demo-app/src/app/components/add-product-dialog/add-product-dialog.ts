import { Component, inject, OnInit, signal } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';
import { ProductService } from '../../services/product.service';
import { Product } from '../../models/product.model';

@Component({
  selector: 'app-add-product-dialog',
  standalone: true,
  imports: [MatDialogModule, MatListModule, MatButtonModule],
  template: `
    <h2 mat-dialog-title>Select a Product</h2>
    <mat-dialog-content>
      <mat-selection-list [multiple]="false" #productList>
        @for (product of allProducts(); track product.id) {
          <mat-list-option [value]="product">
            {{ product.name }} - \${{ product.price }}
          </mat-list-option>
        }
      </mat-selection-list>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="close()">Cancel</button>
      <button mat-flat-button color="primary"
              [disabled]="!productList.selectedOptions.selected[0]"
              (click)="confirm(productList.selectedOptions.selected[0]?.value)">
        Add to Order
      </button>
    </mat-dialog-actions>
  `
})
export class AddProductDialogComponent implements OnInit {
  private productService = inject(ProductService);
  private dialogRef = inject(MatDialogRef<AddProductDialogComponent>);

  allProducts = signal<Product[]>([]);

  ngOnInit() {
    // Load all available products so the customer can choose
    this.productService.getAll().subscribe(data => this.allProducts.set(data));
  }

  confirm(product: Product) {
    if (product) this.dialogRef.close(product);
  }

  close() { this.dialogRef.close(); }
}
