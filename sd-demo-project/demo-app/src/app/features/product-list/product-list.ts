import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { Router } from '@angular/router';

import { ProductFormDialogComponent } from '../../components/product-form-dialog/product-form-dialog';
import { CreateProductDto, Product, UpdateProductDto } from '../../models/product.model';
import { ProductListStore } from './product-list.store';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatButtonModule,
    MatIconModule, MatToolbarModule, MatInputModule, MatFormFieldModule,
    MatDialogModule,
    ProductFormDialogComponent
  ],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProductListComponent {
  private readonly store = inject(ProductListStore);
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);

  protected readonly isAdmin = localStorage.getItem('userRole') === 'ADMIN';
  protected readonly isLoading = this.store.isLoading;
  protected readonly hasError = this.store.hasError;

  protected readonly displayedColumns = this.isAdmin
    ? ['name', 'price', 'actions']
    : ['name', 'price'];

  searchName = signal('');
  maxPrice = signal<number | null>(null);

  filteredProducts = computed(() => {
    const list = this.store.products();
    const nameFilter = this.searchName().toLowerCase();
    const priceFilter = this.maxPrice();

    return list.filter(product => {
      const matchesName = product.name.toLowerCase().includes(nameFilter);
      const matchesPrice = priceFilter === null || priceFilter === 0 || product.price <= priceFilter;
      return matchesName && matchesPrice;
    });
  });

  constructor() {
    this.store.load();
  }

  logout() {
    localStorage.clear();
    void this.router.navigate(['/login']);
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open(ProductFormDialogComponent, {
      data: { title: 'Create Product' },
      width: '400px'
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) {
        // Casting with 'as' satisfies the ESLint "Unsafe argument" rule
        this.store.create(result as CreateProductDto);
      }
    });
  }

  openEditDialog(product: Product): void {
    if (!this.isAdmin) return;
    const dialogRef = this.dialog.open(ProductFormDialogComponent, {
      data: { title: 'Edit Product', initialValue: product },
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result) this.store.update(product.id, result as UpdateProductDto);
    });
  }

  deleteProduct(id: string) {
    if (!this.isAdmin) return;
    if(confirm('Are you sure you want to delete this product?')) {
      this.store.remove(id);
    }
  }

  goToPeople() {
    void this.router.navigate(['/people']);
  }
}
