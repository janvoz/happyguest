import { Pipe, PipeTransform } from '@angular/core';
import { PropertyFaqItem } from '../models';

@Pipe({
  name: 'faqFilter',
  pure: true
})
export class FaqFilterPipe implements PipeTransform {
  transform(items: PropertyFaqItem[] | null | undefined, searchTerm: string | null | undefined): PropertyFaqItem[] {
    const list = items ?? [];
    const term = (searchTerm ?? '').trim().toLowerCase();
    if (!term) {
      return list;
    }
    return list.filter((item) =>
      (item.question ?? '').toLowerCase().includes(term)
      || (item.answer ?? '').toLowerCase().includes(term)
    );
  }
}
