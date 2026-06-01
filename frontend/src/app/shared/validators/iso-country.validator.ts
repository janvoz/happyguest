import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { ISO_COUNTRIES } from '../iso-countries';

const VALID_CODES = new Set(ISO_COUNTRIES.map((c) => c.code.toUpperCase()));

/**
 * Validates that the control value is a recognised ISO 3166-1 alpha-2 country code.
 * Accepts either the bare code (e.g. "US") or a display string like "United States (US)".
 * An empty value is considered valid (combine with Validators.required for mandatory fields).
 */
export function isoCountryCodeValidator(): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const raw = String(control.value ?? '').trim();
    if (!raw) {
      return null;
    }
    // Accept bare 2-letter code or extract from "Name (XX)" display format
    const match = raw.match(/\(([A-Za-z]{2})\)\s*$/);
    const code = match ? match[1].toUpperCase() : raw.toUpperCase();
    return VALID_CODES.has(code) ? null : { invalidCountryCode: true };
  };
}
