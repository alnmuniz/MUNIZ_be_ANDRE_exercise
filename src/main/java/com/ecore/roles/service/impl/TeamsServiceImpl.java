package com.ecore.roles.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecore.roles.client.TeamsClient;
import com.ecore.roles.client.model.Team;
import com.ecore.roles.exception.ResourceNotFoundException;
import com.ecore.roles.service.TeamsService;

import static java.util.Optional.ofNullable;

@Service
public class TeamsServiceImpl implements TeamsService {

    private final TeamsClient teamsClient;

    @Autowired
    public TeamsServiceImpl(TeamsClient teamsClient) {
        this.teamsClient = teamsClient;
    }

    public Team getTeam(UUID id) {
        return teamsClient.getTeam(id).getBody();
    }

    public List<Team> getTeams() {
        return teamsClient.getTeams().getBody();
    }

    public boolean isMemberOfTeam(UUID teamId, UUID userId) {

        Team team = ofNullable(getTeam(teamId))
                .orElseThrow(() -> new ResourceNotFoundException(Team.class, teamId));

        return (team.getTeamLeadId().equals(userId) || team.getTeamMemberIds().contains(userId));
    }
}
