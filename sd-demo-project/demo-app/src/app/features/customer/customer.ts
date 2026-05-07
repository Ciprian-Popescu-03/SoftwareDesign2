import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog'; // Added MatDialogModule

import { OrderService } from '../../services/order.service';
import { Order } from '../../models/order.model';
import { Product } from '../../models/product.model'; // <-- THIS FIXES THE ERRORS
import { AddProductDialogComponent } from '../../components/add-product-dialog/add-product-dialog';

@Component({
  selector: 'app-customer',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatDialogModule // <-- Necessary for opening dialogs from this page
  ],
  templateUrl: './customer.html',
})
export class CustomerComponent implements OnInit {
  private readonly dialog = inject(MatDialog);
  private readonly router = inject(Router);
  private readonly orderService = inject(OrderService);

  orders = signal<Order[]>([]);
  displayedColumns = ['id', 'productCount', 'total', 'actions'];

  ngOnInit() {
    console.log("Customer component loaded!"); // Debug: Check your console (F12)
    this.loadOrders();
  }

  loadOrders() {
    const pId = localStorage.getItem('userId');
    if (!pId) return;

    this.orderService.getOrdersByPersonId(pId).subscribe({
      next: (data) => {
        console.log("Orders loaded from server:", data);
        this.orders.set(data); // This updates the UI table
      },
      error: (err) => console.error("Could not fetch orders:", err)
    });
  }

  createNewOrder() {
    const dialogRef = this.dialog.open(AddProductDialogComponent);

    dialogRef.afterClosed().subscribe((selectedProduct: Product | undefined) => {
      if (selectedProduct?.id) {
        const pId = localStorage.getItem('userId') || '';

        this.orderService.createOrder(pId, selectedProduct.id).subscribe({
          next: () => {
            // CRITICAL: We must call loadOrders() here to refresh the list!
            this.loadOrders();
          },
          error: (err) => console.error("Order creation failed:", err)
        });
      }
    });
  }

  deleteOrder(id: string) {
    if(confirm('Cancel this order?')) {
      this.orderService.deleteOrder(id).subscribe(() => this.loadOrders());
    }
  }

  goToProducts() {
    void this.router.navigate(['/products']);
  }

  logout() {
    localStorage.clear();
    void this.router.navigate(['/login']);
  }

  openAddProductDialog(orderId: string) {
    const dialogRef = this.dialog.open(AddProductDialogComponent);

    dialogRef.afterClosed().subscribe((selectedProduct: Product | undefined) => {
      // If a product was chosen and it has an ID
      if (selectedProduct?.id) {
        this.orderService.addProductToOrder(orderId, selectedProduct.id).subscribe({
          next: () => {
            this.loadOrders(); // Refresh table
          },
          error: () => alert("Could not add product to order.")
        });
      }
    });
  }
}
