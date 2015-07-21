-- Event Log table --
create table eventlog
( 
	  event_id int not null,
	  process_id int not null,
	  case_id    int not null,
      resources varchar(100),
      activity varchar(100),
      eventtype varchar(100),
      time_stamp   timestamp,
      constraint event_pk primary key(event_id)
);