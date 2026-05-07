import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { CreateProductDto, Product, UpdateProductDto } from '../../models/product.model';
import { ProductService } from '../../services/product.service';

@Injectable({ providedIn: 'root' })
export class ProductListStore {
  private readonly productService = inject(ProductService);
  private readonly pendingRequests = signal(0);

  readonly products = signal<Product[]>([]);
  readonly hasError = signal(false);
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  load(): void {
    this.hasError.set(false);
    this.beginRequest();
    this.productService.getAll()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.products.set(data),
        error: () => this.hasError.set(true),
      });
  }

  // --- Added CREATE ---
  create(dto: CreateProductDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.productService.create(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => {
          // This adds the new product to the table instantly
          this.products.update((list) => [...list, created]);
        },
        error: (err) => {
          console.error('Backend rejected product:', err);
          this.hasError.set(true);
        }
      });
  }

  // --- Added UPDATE ---
  update(id: string, dto: UpdateProductDto): void {
    this.hasError.set(false);
    this.beginRequest();
    this.productService.update(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) =>
          this.products.update((list) =>
            list.map((product) => (product.id === updated.id ? updated : product)),
          ),
        error: () => this.hasError.set(true),
      });
  }

  remove(id: string): void {
    this.hasError.set(false);
    this.beginRequest();
    this.productService.delete(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => this.products.update((list) => list.filter((p) => p.id !== id)),
        error: () => this.hasError.set(true),
      });
  }
}
