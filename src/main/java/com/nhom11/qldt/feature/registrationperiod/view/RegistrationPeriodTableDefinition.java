package com.nhom11.qldt.feature.registrationperiod.view;
import java.util.List;
import org.springframework.stereotype.Component;
import com.nhom11.qldt.core.table.*;
import com.nhom11.qldt.feature.registrationperiod.model.entity.RegistrationPeriod;
@Component public class RegistrationPeriodTableDefinition extends BaseTableDefinition<RegistrationPeriod> {
 public List<ColumnDef> columns(){return List.of(new ColumnDef("id","registration_periods.column.id",item->String.valueOf(((RegistrationPeriod)item).getId()),false,false,true),new ColumnDef("name","registration_periods.column.name",item->((RegistrationPeriod)item).getName(),true,true,true),new ColumnDef("periodType","registration_periods.column.type",item->((RegistrationPeriod)item).getPeriodType().name(),false,true,true));}
 public List<ActionDef> actions(String baseUrl){return List.of(new ActionDef.Link("registration_periods.action.edit","bi:pencil-square",item->baseUrl+"/"+((RegistrationPeriod)item).getId()+"/edit"),new ActionDef.Delete("registration_periods.action.delete","bi:trash",item->baseUrl+"/"+((RegistrationPeriod)item).getId(),item->((RegistrationPeriod)item).getName()));}
}
