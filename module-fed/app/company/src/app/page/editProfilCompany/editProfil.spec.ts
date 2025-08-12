import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EditProfil } from './editProfil';

describe('EditProfil', () => {
  let component: EditProfil;
  let fixture: ComponentFixture<EditProfil>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditProfil],
    }).compileComponents();

    fixture = TestBed.createComponent(EditProfil);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
