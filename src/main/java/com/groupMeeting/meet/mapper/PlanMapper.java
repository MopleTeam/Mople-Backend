package com.groupMeeting.meet.mapper;

import com.groupMeeting.dto.response.meet.plan.PlanViewResponse;
import com.groupMeeting.entity.meet.plan.MeetPlan;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlanMapper {
    PlanMapper INSTANCE = Mappers.getMapper(PlanMapper.class);

    @Named("planView")
    @Mapping(source = "plan.id", target = "planId")
    @Mapping(source = "plan.meet.id", target = "meetId")
    @Mapping(source = "plan.meet.name", target = "meetName")
    @Mapping(source = "plan.meet.meetImage", target = "meetImage")
    @Mapping(source = "plan.name", target = "planName")
    @Mapping(target = "creatorId", expression = "java(plan.getCreator().getId())")
    @Mapping(target = "planMemberCount", expression = "java(plan.getParticipants().size())")
    @Mapping(source = "plan.planTime", target = "planTime")
    @Mapping(source = "plan.address", target = "planAddress")
    @Mapping(source = "plan.title", target = "title")
    @Mapping(source = "plan.latitude", target = "lat")
    @Mapping(source = "plan.longitude", target = "lot")
    @Mapping(source = "plan.pop", target = "pop")
    PlanViewResponse getPlanView(MeetPlan plan);


    @IterableMapping(qualifiedByName = "planView")
    List<PlanViewResponse> getPlanViews(List<MeetPlan> plans);

}
