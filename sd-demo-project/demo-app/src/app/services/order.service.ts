import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Order } from '../models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8081/order';

  getOrdersByEmail(email: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API_URL}/customer/${email}`);
  }

  createOrder(personId: string, productId: string): Observable<Order> {
    const payload = {
      personId: personId,
      productIds: [productId]
    };
    return this.http.post<Order>(this.API_URL, payload);
  }

  deleteOrder(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }

  addProductToOrder(orderId: string, productId: string): Observable<Order> {
    return this.http.post<Order>(`${this.API_URL}/${orderId}/product/${productId}`, {});
  }
  // Make sure this path matches your @GetMapping in Spring Boot's OrderController
  getOrdersByPersonId(personId: string): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.API_URL}/person/${personId}`);
  }
}
