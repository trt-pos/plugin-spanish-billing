create table sb_spain_bill
(
    bill_number varchar(255) not null,
    bill_date   timestamp    not null,
    receipt_id  int,
    constraint PK_SPAIN_BILL primary key (bill_number),
    constraint UQ_SPAIN_BILL_RECEIPT unique (receipt_id),
    constraint FK_SPAIN_BILL_RECEIPT foreign key (receipt_id) references cr_receipt (id)
);