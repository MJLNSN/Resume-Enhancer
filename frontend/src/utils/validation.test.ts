import { describe, test, expect } from 'vitest';

// Simple validation utility functions
export const validateEmail = (email: string): boolean => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validatePassword = (password: string): boolean => {
  return password.length >= 8;
};

export const validateFullName = (name: string): boolean => {
  return name.trim().length >= 2;
};

// Test functions
describe('Validation Utils', () => {
  test('validateEmail should return true for valid emails', () => {
    expect(validateEmail('test@example.com')).toBe(true);
    expect(validateEmail('user.name@domain.co.uk')).toBe(true);
  });

  test('validateEmail should return false for invalid emails', () => {
    expect(validateEmail('invalid-email')).toBe(false);
    expect(validateEmail('test@')).toBe(false);
    expect(validateEmail('@example.com')).toBe(false);
  });

  test('validatePassword should return true for passwords >= 8 chars', () => {
    expect(validatePassword('password123')).toBe(true);
    expect(validatePassword('12345678')).toBe(true);
  });

  test('validatePassword should return false for passwords < 8 chars', () => {
    expect(validatePassword('1234567')).toBe(false);
    expect(validatePassword('pass')).toBe(false);
  });

  test('validateFullName should return true for valid names', () => {
    expect(validateFullName('John Doe')).toBe(true);
    expect(validateFullName('Jane')).toBe(true);
  });

  test('validateFullName should return false for invalid names', () => {
    expect(validateFullName('')).toBe(false);
    expect(validateFullName(' ')).toBe(false);
    expect(validateFullName('J')).toBe(false);
  });
});
