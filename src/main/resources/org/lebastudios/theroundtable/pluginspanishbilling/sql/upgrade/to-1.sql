create table sb_spain_bill
(
    id         varchar(20) not null,
    type       varchar(10)  not null,
    receipt_id int,
    constraint PK_SB_SPAIN_BILL primary key (id),
    constraint CK_SB_SPAIN_BILL_TYPE check ( type in ('RECT', 'SELL') ),
    constraint UQ_SB_SPAIN_BILL_RECEIPT unique (receipt_id),
    constraint FK_SB_SPAIN_BILL_RECEIPT foreign key (receipt_id) references cr_receipt (id)
);
-- DELIMITER
create table sb_billing_config
(
    type             varchar(10)  not null,
    series           varchar(10)  not null,
    delimiter        char         not null,
    next_number      varchar(10)      not null,
    last_bill_number varchar(100) not null,
    constraint PK_SB_BILLING_CONFIG primary key (type),
    constraint CK_SB_BILLING_CONFIG_TYPE check ( type in ('RECT', 'SELL') ),
    constraint CK_SB_BILLING_CONFIG_NEXT_NUMBER check ( next_number > 0 ),
    constraint CK_SB_BILLING_CONFIG_DELIMITER check ( delimiter in ('-', '/', '\', ' ', ':') ),
    constraint UQ_SB_BILLING_CONFIG_SERIES unique (series)
);
-- DELIMITER
insert into sb_billing_config (type, series, delimiter, next_number, last_bill_number)
values ('SELL', 'A', '-', 1, 'No emitido'),
       ('RECT', 'R', '-', 1, 'No emitido');