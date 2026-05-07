export interface Product {
  id: string;
  name: string;
  price: number;
}

export type CreateProductDto = Omit<Product, 'id'>;
export type UpdateProductDto = Omit<Product, 'id'>;
