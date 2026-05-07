import { Product } from './product.model';

export interface Order {
  id: string;
  customerEmail: string;
  products: Product[];
  totalPrice: number;
}
