package com.willythedev.librarymanagementsystem.service;

import com.willythedev.librarymanagementsystem.exception.ItemExistsException;
import com.willythedev.librarymanagementsystem.exception.ItemNotFoundException;
import com.willythedev.librarymanagementsystem.model.Book;
import com.willythedev.librarymanagementsystem.model.BorrowingRecord;
import com.willythedev.librarymanagementsystem.model.Patron;
import com.willythedev.librarymanagementsystem.repository.BorrowingRecordRepository;
import com.willythedev.librarymanagementsystem.util.EntityToDtoConverter;
import com.willythedev.librarymanagementsystem.wrapper.UniversalResponse;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BorrowingRecordService {
  private final BookService bookService;
  private final BorrowingRecordRepository borrowingRecordRepository;
  private final PatronService patronService;

  @Transactional
  public UniversalResponse borrowBook(String bookId, String patronId) {
    Book book = bookService.getBookById(bookId);
    Patron patron = patronService.getPatronById(patronId);
    Optional<BorrowingRecord> borrowingRecordOptional =
        borrowingRecordRepository.findFirstByBookIdAndPatronIdAndReturnedFalse(bookId, patronId);

    if (borrowingRecordOptional.isPresent()) {
      throw new ItemExistsException("Book has already been borrowed");
    }

    BorrowingRecord borrowingRecord =
        BorrowingRecord.builder().book(book).patron(patron).returned(false).build();
    BorrowingRecord savedRecord = borrowingRecordRepository.save(borrowingRecord);

    return new UniversalResponse(
        200,
        "Record saved successfully",
        EntityToDtoConverter.convertBorrowingRecordToDto.apply(savedRecord));
  }

  @Transactional
  public UniversalResponse returnBook(String bookId, String patronId) {
    Optional<BorrowingRecord> borrowingRecordOptional =
        borrowingRecordRepository.findFirstByBookIdAndPatronIdAndReturnedFalse(bookId, patronId);

    if (borrowingRecordOptional.isEmpty()) {
      throw new ItemNotFoundException("Borrowing record not found");
    }

    BorrowingRecord borrowingRecord = borrowingRecordOptional.get();
    borrowingRecord.setReturned(true);
    BorrowingRecord savedRecord = borrowingRecordRepository.save(borrowingRecord);

    return new UniversalResponse(
        200,
        "Record updated successfully",
        EntityToDtoConverter.convertBorrowingRecordToDto.apply(savedRecord));
  }
}
