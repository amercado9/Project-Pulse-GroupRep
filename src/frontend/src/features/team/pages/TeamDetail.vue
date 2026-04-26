<template>
  <v-container>
    <v-btn variant="text" prepend-icon="mdi-arrow-left" class="mb-4" @click="router.push({ name: 'teams' })">
      Back to Teams
    </v-btn>

    <v-row v-if="loading">
      <v-col class="text-center"><v-progress-circular indeterminate /></v-col>
    </v-row>

    <template v-else-if="team">
      <v-row class="mb-2" align="center">
        <v-col>
          <h2 class="text-h5 font-weight-bold">{{ team.teamName }}</h2>
        </v-col>
        <v-col v-if="isAdmin" cols="auto">
          <div class="admin-actions">
            <v-btn color="primary" prepend-icon="mdi-pencil" @click="openEditDialog">Edit Team</v-btn>
            <v-btn color="error" variant="outlined" prepend-icon="mdi-delete" @click="openDeleteDialog">
              Delete Team
            </v-btn>
          </div>
        </v-col>
        <v-col cols="auto">
          <v-btn variant="outlined" prepend-icon="mdi-file-chart" @click="openWarDialog">WAR Report</v-btn>
        </v-col>
        <v-col cols="auto">
          <v-chip color="primary" variant="tonal">
            {{ team.sectionName }}
          </v-chip>
        </v-col>
      </v-row>

      <v-card variant="outlined" class="mb-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Team Info</v-card-title>
        <v-card-text>
          <v-row>
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Description</div>
              <div>{{ team.teamDescription ?? '—' }}</div>
            </v-col>
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">Website</div>
              <a
                v-if="team.teamWebsiteUrl"
                :href="team.teamWebsiteUrl"
                target="_blank"
                rel="noopener noreferrer"
              >
                {{ team.teamWebsiteUrl }}
              </a>
              <div v-else>—</div>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <v-row>
        <v-col cols="12" md="6">
          <v-card variant="outlined" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2 d-flex align-center">
              <span>Team Members</span>
              <v-spacer />
              <v-btn
                v-if="isAdmin && team.teamMembers.length > 0"
                size="small"
                color="error"
                variant="outlined"
                prepend-icon="mdi-account-remove"
                @click="openRemoveDialog"
              >
                Remove Student
              </v-btn>
            </v-card-title>
            <v-card-text>
              <v-alert v-if="team.teamMembers.length === 0" type="info" variant="tonal" density="compact">
                No team members assigned.
              </v-alert>
              <div v-else>
                <template v-for="member in team.teamMembers" :key="member.studentId">
                  <v-menu v-if="isAdmin || isInstructor" location="bottom start">
                    <template #activator="{ props: menuProps }">
                      <v-chip
                        v-bind="menuProps"
                        size="small"
                        class="mr-1 mb-1"
                        color="primary"
                        variant="tonal"
                        style="cursor:pointer"
                      >
                        {{ member.fullName }}
                      </v-chip>
                    </template>
                    <v-list density="compact">
                      <v-list-item prepend-icon="mdi-file-chart" title="WAR Report" @click="openStudentWarDialog(member)" />
                      <v-list-item prepend-icon="mdi-account-star" title="Peer Eval Report" @click="openStudentPeerEvalDialog(member)" />
                    </v-list>
                  </v-menu>
                  <v-chip v-else size="small" class="mr-1 mb-1">{{ member.fullName }}</v-chip>
                </template>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
        <v-col cols="12" md="6">
          <v-card variant="outlined" class="fill-height">
            <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2 d-flex align-center">
              <span>Instructors</span>
              <v-spacer />
              <v-btn
                v-if="isAdmin && team.teamInstructors.length > 0"
                size="small"
                color="error"
                variant="outlined"
                prepend-icon="mdi-account-remove"
                @click="openRemoveInstructorDialog"
              >
                Remove Instructor
              </v-btn>
            </v-card-title>
            <v-card-text>
              <v-alert v-if="team.teamInstructors.length === 0" type="info" variant="tonal" density="compact">
                No instructors assigned.
              </v-alert>
              <div v-else>
                <v-chip
                  v-for="instructor in team.teamInstructors"
                  :key="instructor.instructorId"
                  size="small"
                  color="primary"
                  variant="tonal"
                  class="mr-1 mb-1"
                >
                  {{ instructor.fullName }}
                </v-chip>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-card v-if="removedStudentNotification" variant="outlined" class="mt-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Notify Student</v-card-title>
        <v-card-text>
          <div class="text-body-2 mb-3">
            Use this summary to notify the student manually about the team removal.
          </div>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Student</th>
                <td>{{ removedStudentNotification.fullName }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ removedStudentNotification.email }}</td>
              </tr>
              <tr>
                <th class="text-left">Previous Team</th>
                <td>{{ removedStudentNotification.teamName }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ removedStudentNotification.sectionName }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>{{ removedStudentNotification.status }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>

      <v-card v-if="removedInstructorNotification" variant="outlined" class="mt-4">
        <v-card-title class="text-subtitle-1 font-weight-bold pa-4 pb-2">Notify Instructor</v-card-title>
        <v-card-text>
          <div class="text-body-2 mb-3">
            Use this summary to notify the instructor manually about the team removal.
          </div>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Instructor</th>
                <td>{{ removedInstructorNotification.fullName }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ removedInstructorNotification.email }}</td>
              </tr>
              <tr>
                <th class="text-left">Previous Team</th>
                <td>{{ removedInstructorNotification.teamName }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ removedInstructorNotification.sectionName }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>{{ removedInstructorNotification.status }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>
    </template>

    <v-dialog v-model="editDialog" max-width="700" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ editStep === 1 ? 'Edit Team' : 'Confirm Team Changes' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="editStep === 1" class="pa-4">
          <v-text-field
            v-model="form.teamName"
            label="Team Name"
            placeholder="e.g. Peer Evaluation Tool team"
            :error-messages="editErrors.teamName"
            class="mb-4"
          />

          <v-textarea
            v-model="form.teamDescription"
            label="Team Description"
            rows="3"
            class="mb-4"
          />

          <v-text-field
            v-model="form.teamWebsiteUrl"
            label="Team Website URL"
            placeholder="https://example.com"
            :error-messages="editErrors.teamWebsiteUrl"
          />
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="info" variant="tonal" class="mb-4">
            Please review the team changes before confirming.
          </v-alert>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team Name</th>
                <td>{{ previewPayload.teamName }}</td>
              </tr>
              <tr>
                <th class="text-left">Description</th>
                <td>{{ previewPayload.teamDescription ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Website</th>
                <td>{{ previewPayload.teamWebsiteUrl ?? '—' }}</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelEdit">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="editStep === 2" variant="outlined" @click="editStep = 1">Modify</v-btn>
          <v-btn
            color="primary"
            :loading="editSaving"
            @click="editStep === 1 ? goToEditPreview() : confirmEdit()"
          >
            {{ editStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="removeDialog" max-width="720" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ removeStep === 1 ? 'Remove Student' : 'Confirm Student Removal' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="removeStep === 1" class="pa-4">
          <v-select
            v-model="selectedStudentId"
            :items="team?.teamMembers ?? []"
            item-title="fullName"
            item-value="studentId"
            label="Student to Remove"
            :error-messages="removeErrors.studentId"
          />

          <v-card v-if="selectedTeamMember" variant="outlined" class="mt-4">
            <v-card-text>
              <div class="text-caption text-medium-emphasis">Selected Student Email</div>
              <div>{{ selectedTeamMember.email }}</div>
            </v-card-text>
          </v-card>
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Please review the student removal before confirming.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Student</th>
                <td>{{ selectedTeamMember?.fullName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ selectedTeamMember?.email ?? '—' }}</td>
              </tr>
            </tbody>
          </v-table>

          <div class="text-subtitle-2 font-weight-bold mb-2">Remaining Team Members</div>
          <v-alert
            v-if="remainingTeamMembers.length === 0"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No team members will remain after this removal.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="member in remainingTeamMembers"
              :key="member.studentId"
              size="small"
              class="mr-1 mb-1"
            >
              {{ member.fullName }}
            </v-chip>
          </div>

          <div class="text-subtitle-2 font-weight-bold mb-2">Manual Notification Preview</div>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Student</th>
                <td>{{ selectedTeamMember?.fullName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ selectedTeamMember?.email ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Previous Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>Removed from team</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelRemove">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="removeStep === 2" variant="outlined" @click="removeStep = 1">Modify</v-btn>
          <v-btn
            color="error"
            :loading="removeSaving"
            @click="removeStep === 1 ? goToRemovePreview() : confirmRemove()"
          >
            {{ removeStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="removeInstructorDialog" max-width="720" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ removeInstructorStep === 1 ? 'Remove Instructor' : 'Confirm Instructor Removal' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="removeInstructorStep === 1" class="pa-4">
          <v-select
            v-model="selectedInstructorId"
            :items="team?.teamInstructors ?? []"
            item-title="fullName"
            item-value="instructorId"
            label="Instructor to Remove"
            :error-messages="removeInstructorErrors.instructorId"
          />

          <v-card v-if="selectedTeamInstructor" variant="outlined" class="mt-4">
            <v-card-text>
              <div class="text-caption text-medium-emphasis">Selected Instructor Email</div>
              <div>{{ selectedTeamInstructor.email }}</div>
            </v-card-text>
          </v-card>
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Please review the instructor removal before confirming.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Instructor</th>
                <td>{{ selectedTeamInstructor?.fullName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ selectedTeamInstructor?.email ?? '—' }}</td>
              </tr>
            </tbody>
          </v-table>

          <div class="text-subtitle-2 font-weight-bold mb-2">Remaining Instructors</div>
          <v-alert
            v-if="remainingTeamInstructors.length === 0"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No instructors will remain after this removal.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="instructor in remainingTeamInstructors"
              :key="instructor.instructorId"
              size="small"
              color="primary"
              variant="tonal"
              class="mr-1 mb-1"
            >
              {{ instructor.fullName }}
            </v-chip>
          </div>

          <div class="text-subtitle-2 font-weight-bold mb-2">Manual Notification Preview</div>
          <v-table density="compact">
            <tbody>
              <tr>
                <th class="text-left">Instructor</th>
                <td>{{ selectedTeamInstructor?.fullName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Email</th>
                <td>{{ selectedTeamInstructor?.email ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Previous Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>Removed from team</td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelRemoveInstructor">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="removeInstructorStep === 2" variant="outlined" @click="removeInstructorStep = 1">Modify</v-btn>
          <v-btn
            color="error"
            :loading="removeInstructorSaving"
            @click="removeInstructorStep === 1 ? goToRemoveInstructorPreview() : confirmRemoveInstructor()"
          >
            {{ removeInstructorStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="760" persistent>
      <v-card>
        <v-card-title class="pa-4">
          {{ deleteStep === 1 ? 'Delete Team' : 'Confirm Team Deletion' }}
        </v-card-title>
        <v-divider />

        <v-card-text v-if="deleteStep === 1" class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Deleting this senior design team is permanent and cannot be undone.
          </v-alert>

          <div class="text-body-2 mb-4">
            If you continue, the system will physically delete this team. Students and instructors will be removed
            from the team automatically. Associated WARs and peer evaluations would also be deleted if those
            records existed.
          </div>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Deletion Strategy</th>
                <td>Physical delete</td>
              </tr>
              <tr>
                <th class="text-left">Current Scope</th>
                <td>Existing team and membership data in the current system</td>
              </tr>
            </tbody>
          </v-table>

          <v-alert type="info" variant="tonal">
            WAR and peer-evaluation modules are not present in this repo yet, so this implementation deletes the
            existing team and membership records now and defers future related-record cleanup until those modules exist.
          </v-alert>
        </v-card-text>

        <v-card-text v-else class="pa-4">
          <v-alert type="warning" variant="tonal" class="mb-4">
            Please review the deletion consequences before confirming.
          </v-alert>

          <v-table density="compact" class="mb-4">
            <tbody>
              <tr>
                <th class="text-left">Section</th>
                <td>{{ team?.sectionName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Team</th>
                <td>{{ team?.teamName ?? '—' }}</td>
              </tr>
              <tr>
                <th class="text-left">Status</th>
                <td>Permanently delete this team</td>
              </tr>
            </tbody>
          </v-table>

          <div class="text-subtitle-2 font-weight-bold mb-2">Students to Remove</div>
          <v-alert
            v-if="!team?.teamMembers.length"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No students are currently assigned to this team.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="member in team.teamMembers"
              :key="member.studentId"
              size="small"
              class="mr-1 mb-1"
            >
              {{ member.fullName }}
            </v-chip>
          </div>

          <div class="text-subtitle-2 font-weight-bold mb-2">Instructors to Remove</div>
          <v-alert
            v-if="!team?.teamInstructors.length"
            type="info"
            variant="tonal"
            density="compact"
            class="mb-4"
          >
            No instructors are currently assigned to this team.
          </v-alert>
          <div v-else class="mb-4">
            <v-chip
              v-for="instructor in team.teamInstructors"
              :key="instructor.instructorId"
              size="small"
              color="primary"
              variant="tonal"
              class="mr-1 mb-1"
            >
              {{ instructor.fullName }}
            </v-chip>
          </div>

          <v-alert type="error" variant="tonal">
            This action cannot be undone.
          </v-alert>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-btn variant="text" @click="cancelDelete">Cancel</v-btn>
          <v-spacer />
          <v-btn v-if="deleteStep === 2" variant="outlined" @click="deleteStep = 1">Modify</v-btn>
          <v-btn
            color="error"
            :loading="deleteSaving"
            @click="deleteStep === 1 ? goToDeletePreview() : confirmDelete()"
          >
            {{ deleteStep === 1 ? 'Preview' : 'Confirm' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- WAR Report Dialog -->
    <v-dialog v-model="warDialog" max-width="900" scrollable>
      <v-card rounded="lg">
        <v-card-title class="pa-6 pb-2">
          <span v-if="warStep === 1">Generate WAR Report</span>
          <span v-else>WAR Report — {{ warSelectedWeek }}</span>
        </v-card-title>

        <v-card-text class="px-6">
          <!-- Step 1: Pick a week -->
          <template v-if="warStep === 1">
            <p class="text-medium-emphasis mb-4">Select the active week to generate the WAR report for <strong>{{ team?.teamName }}</strong>.</p>
            <v-select
              v-model="warSelectedWeek"
              :items="activeWeeks"
              label="Active Week"
              variant="outlined"
              density="comfortable"
              :rules="[(v) => !!v || 'Please select a week.']"
            />
            <v-alert v-if="!activeWeeks.length" type="warning" variant="tonal" class="mt-2">
              No active weeks configured for this section.
            </v-alert>
          </template>

          <!-- Step 2: Report -->
          <template v-else>
            <template v-if="warReport && warReport.studentReports.length">
              <div v-for="student in warReport.studentReports" :key="student.studentId" class="mb-6">
                <div class="d-flex align-center mb-2 gap-2">
                  <span class="font-weight-bold">{{ student.studentName }}</span>
                  <v-chip v-if="!student.submitted" color="error" size="small" variant="tonal">Did not submit</v-chip>
                </div>
                <template v-if="student.submitted">
                  <v-table density="compact">
                    <thead>
                      <tr>
                        <th>Activity Category</th>
                        <th>Planned Activity</th>
                        <th>Description</th>
                        <th>Planned Hrs</th>
                        <th>Actual Hrs</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="(row, i) in student.activities" :key="i">
                        <td>{{ row.activityCategory }}</td>
                        <td>{{ row.plannedActivity }}</td>
                        <td>{{ row.description ?? '—' }}</td>
                        <td>{{ row.plannedHours ?? '—' }}</td>
                        <td>{{ row.actualHours ?? '—' }}</td>
                        <td>
                          <v-chip :color="row.status === 'Done' ? 'success' : 'warning'" size="small" variant="tonal">
                            {{ row.status }}
                          </v-chip>
                        </td>
                      </tr>
                    </tbody>
                  </v-table>
                </template>
              </div>
            </template>
            <v-alert v-else type="info" variant="tonal">
              No activity data found for week {{ warSelectedWeek }}.
            </v-alert>
          </template>
        </v-card-text>

        <v-card-actions class="px-6 pb-4">
          <v-spacer />
          <v-btn variant="outlined" @click="closeWarDialog">Close</v-btn>
          <v-btn v-if="warStep === 1" color="primary" :disabled="!warSelectedWeek || !activeWeeks.length" :loading="warLoading" @click="generateWarReport">
            Generate
          </v-btn>
          <v-btn v-else variant="outlined" @click="warStep = 1">Back</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Student WAR Report Dialog (UC-34) -->
    <v-dialog v-model="studentWarDialog" max-width="900" persistent>
      <v-card>
        <v-card-title class="pa-6 pb-2">
          <span class="text-h6">WAR Report — {{ selectedStudent?.fullName }}</span>
        </v-card-title>

        <v-card-text class="pa-6">
          <template v-if="studentWarStep === 1">
            <p class="mb-4 text-body-2">Select the week range to generate the WAR report for this student.</p>
            <v-row>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="studentWarStartWeek"
                  :items="activeWeeks"
                  label="Start Week"
                  density="compact"
                  variant="outlined"
                  :disabled="!activeWeeks.length"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="studentWarEndWeek"
                  :items="activeWeeks"
                  label="End Week"
                  density="compact"
                  variant="outlined"
                  :disabled="!activeWeeks.length"
                />
              </v-col>
            </v-row>
          </template>

          <template v-else>
            <div class="text-subtitle-1 font-weight-bold mb-4">
              {{ selectedStudent?.fullName }} — {{ studentWarReport?.startWeek }} to {{ studentWarReport?.endWeek }}
            </div>
            <template v-if="studentWarReport && studentWarReport.weekReports.length > 0">
              <div v-for="weekReport in studentWarReport.weekReports" :key="weekReport.week" class="mb-6">
                <div class="text-subtitle-2 font-weight-medium mb-2">Week {{ weekReport.week }}</div>
                <v-table density="compact">
                  <thead>
                    <tr>
                      <th>Category</th>
                      <th>Planned Activity</th>
                      <th>Description</th>
                      <th>Planned Hrs</th>
                      <th>Actual Hrs</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(row, idx) in weekReport.activities" :key="idx">
                      <td>{{ row.activityCategory }}</td>
                      <td>{{ row.plannedActivity }}</td>
                      <td>{{ row.description ?? '—' }}</td>
                      <td>{{ row.plannedHours ?? '—' }}</td>
                      <td>{{ row.actualHours ?? '—' }}</td>
                      <td>{{ row.status }}</td>
                    </tr>
                  </tbody>
                </v-table>
              </div>
            </template>
            <v-alert v-else type="info" variant="tonal">
              No activity data found for the selected period.
            </v-alert>
          </template>
        </v-card-text>

        <v-card-actions class="px-6 pb-4">
          <v-spacer />
          <v-btn variant="outlined" @click="closeStudentWarDialog">Close</v-btn>
          <v-btn
            v-if="studentWarStep === 1"
            color="primary"
            :disabled="!studentWarStartWeek || !studentWarEndWeek || !activeWeeks.length"
            :loading="studentWarLoading"
            @click="generateStudentWarReport"
          >
            Generate
          </v-btn>
          <v-btn v-else variant="outlined" @click="studentWarStep = 1">Back</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- Student Peer Eval Report Dialog (UC-33) -->
    <v-dialog v-model="studentPeerEvalDialog" max-width="900" persistent>
      <v-card>
        <v-card-title class="pa-6 pb-2">
          <span class="text-h6">Peer Eval Report — {{ selectedStudentForPeerEval?.fullName }}</span>
        </v-card-title>

        <v-card-text class="pa-6">
          <template v-if="studentPeerEvalStep === 1">
            <p class="mb-4 text-body-2">Select the week range to generate the peer evaluation report for this student.</p>
            <v-row>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="studentPeerEvalStartWeek"
                  :items="activeWeeks"
                  label="Start Week"
                  density="compact"
                  variant="outlined"
                  :disabled="!activeWeeks.length"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="studentPeerEvalEndWeek"
                  :items="activeWeeks"
                  label="End Week"
                  density="compact"
                  variant="outlined"
                  :disabled="!activeWeeks.length"
                />
              </v-col>
            </v-row>
          </template>

          <template v-else-if="studentPeerEvalReport">
            <div class="text-subtitle-1 font-weight-bold mb-4">
              {{ selectedStudentForPeerEval?.fullName }} — {{ studentPeerEvalReport.startWeek }} to {{ studentPeerEvalReport.endWeek }}
            </div>
            <template v-if="studentPeerEvalReport.weekReports.length > 0">
              <div v-for="weekReport in studentPeerEvalReport.weekReports" :key="weekReport.week" class="mb-6">
                <div class="text-subtitle-2 font-weight-medium mb-2">
                  Week {{ weekReport.week }}
                  <span v-if="weekReport.grade !== null" class="text-medium-emphasis ml-2">
                    Grade: {{ Math.round(weekReport.grade) }}/{{ Math.round(weekReport.maxGrade!) }}
                  </span>
                </div>
                <v-table density="compact">
                  <thead>
                    <tr>
                      <th>Commented by</th>
                      <th>Score</th>
                      <th>Public Comments</th>
                      <th>Private Comments</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(ev, idx) in weekReport.evaluations" :key="idx">
                      <td>{{ ev.evaluatorName }}</td>
                      <td>{{ Math.round(ev.totalScore) }}/{{ Math.round(ev.maxScore) }}</td>
                      <td>{{ ev.publicComment ?? '—' }}</td>
                      <td>{{ ev.privateComment ?? '—' }}</td>
                    </tr>
                  </tbody>
                </v-table>
              </div>
            </template>
            <v-alert v-else type="info" variant="tonal">
              No peer evaluation data found for the selected period.
            </v-alert>
          </template>
        </v-card-text>

        <v-card-actions class="px-6 pb-4">
          <v-spacer />
          <v-btn variant="outlined" @click="closeStudentPeerEvalDialog">Close</v-btn>
          <v-btn
            v-if="studentPeerEvalStep === 1"
            color="primary"
            :disabled="!studentPeerEvalStartWeek || !studentPeerEvalEndWeek || !activeWeeks.length"
            :loading="studentPeerEvalLoading"
            @click="generateStudentPeerEvalReport"
          >
            Generate
          </v-btn>
          <v-btn v-else variant="outlined" @click="studentPeerEvalStep = 1">Back</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="snackbar.show" :color="snackbar.color" timeout="3000">
      {{ snackbar.message }}
    </v-snackbar>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserInfoStore } from '@/stores/userInfo'
import { deleteTeam, getTeam, removeInstructorFromTeam, removeStudentFromTeam, updateTeam } from '../services/teamService'
import { useTeamNotificationsStore } from '../stores/teamNotifications'
import { getSection } from '@/features/section/services/sectionService'
import { getTeamWarReport, getStudentWarReport, getInstructorStudentPeerEvalReport } from '@/features/report/services/reportService'
import type {
  TeamDeletionNotification,
  TeamDetail,
  TeamInstructorDetail,
  TeamMemberDetail,
  UpdateTeamRequest
} from '../services/teamTypes'
import type { TeamWarReportResponse, StudentWarReportResponse, InstructorStudentPeerEvalReportResponse } from '@/features/report/services/reportTypes'

interface RemovedStudentNotification {
  fullName: string
  email: string
  teamName: string
  sectionName: string
  status: string
}

interface RemovedInstructorNotification {
  fullName: string
  email: string
  teamName: string
  sectionName: string
  status: string
}

const route = useRoute()
const router = useRouter()
const userInfoStore = useUserInfoStore()
const teamNotificationsStore = useTeamNotificationsStore()

const team = ref<TeamDetail | null>(null)
const loading = ref(false)
const editDialog = ref(false)
const editStep = ref(1)
const editSaving = ref(false)
const editErrors = ref<{ teamName?: string; teamWebsiteUrl?: string }>({})
const removeDialog = ref(false)
const removeStep = ref(1)
const removeSaving = ref(false)
const removeErrors = ref<{ studentId?: string }>({})
const selectedStudentId = ref<number | null>(null)
const removeInstructorDialog = ref(false)
const removeInstructorStep = ref(1)
const removeInstructorSaving = ref(false)
const removeInstructorErrors = ref<{ instructorId?: string }>({})
const selectedInstructorId = ref<number | null>(null)
const deleteDialog = ref(false)
const deleteStep = ref(1)
const deleteSaving = ref(false)
const removedStudentNotification = ref<RemovedStudentNotification | null>(null)
const removedInstructorNotification = ref<RemovedInstructorNotification | null>(null)
const snackbar = ref({ show: false, message: '', color: 'success' })

const warDialog = ref(false)
const warStep = ref(1)
const warLoading = ref(false)
const warSelectedWeek = ref<string>('')
const activeWeeks = ref<string[]>([])
const warReport = ref<TeamWarReportResponse | null>(null)

const studentWarDialog = ref(false)
const studentWarStep = ref(1)
const studentWarLoading = ref(false)
const studentWarStartWeek = ref<string>('')
const studentWarEndWeek = ref<string>('')
const selectedStudent = ref<{ studentId: number; fullName: string } | null>(null)
const studentWarReport = ref<StudentWarReportResponse | null>(null)

const studentPeerEvalDialog = ref(false)
const studentPeerEvalStep = ref(1)
const studentPeerEvalLoading = ref(false)
const studentPeerEvalStartWeek = ref<string>('')
const studentPeerEvalEndWeek = ref<string>('')
const selectedStudentForPeerEval = ref<{ studentId: number; fullName: string } | null>(null)
const studentPeerEvalReport = ref<InstructorStudentPeerEvalReportResponse | null>(null)

const emptyForm = () => ({
  teamName: '',
  teamDescription: '',
  teamWebsiteUrl: ''
})

const form = ref(emptyForm())

const isAdmin = computed(() => userInfoStore.isAdmin)
const isInstructor = computed(() => userInfoStore.isInstructor)

const previewPayload = computed<UpdateTeamRequest>(() => ({
  teamName: form.value.teamName.trim(),
  teamDescription: normalizeOptional(form.value.teamDescription),
  teamWebsiteUrl: normalizeOptional(form.value.teamWebsiteUrl)
}))

const selectedTeamMember = computed<TeamMemberDetail | null>(() =>
  team.value?.teamMembers.find((member) => member.studentId === selectedStudentId.value) ?? null
)

const selectedTeamInstructor = computed<TeamInstructorDetail | null>(() =>
  team.value?.teamInstructors.find((instructor) => instructor.instructorId === selectedInstructorId.value) ?? null
)

const remainingTeamMembers = computed(() =>
  team.value?.teamMembers.filter((member) => member.studentId !== selectedStudentId.value) ?? []
)

const remainingTeamInstructors = computed(() =>
  team.value?.teamInstructors.filter((instructor) => instructor.instructorId !== selectedInstructorId.value) ?? []
)

onMounted(async () => {
  loading.value = true
  try {
    const teamId = Number(route.params.id)
    const res = await getTeam(teamId) as any
    if (res.flag && res.data) {
      team.value = res.data
    } else {
      router.replace({ name: 'not-found' })
    }
  } catch {
    router.replace({ name: 'not-found' })
  } finally {
    loading.value = false
  }
})

function openEditDialog() {
  if (!team.value) return

  form.value = {
    teamName: team.value.teamName,
    teamDescription: team.value.teamDescription ?? '',
    teamWebsiteUrl: team.value.teamWebsiteUrl ?? ''
  }
  editErrors.value = {}
  editStep.value = 1
  editDialog.value = true
}

function cancelEdit() {
  editDialog.value = false
}

function openRemoveDialog() {
  selectedStudentId.value = null
  removeErrors.value = {}
  removeStep.value = 1
  removeDialog.value = true
}

function cancelRemove() {
  removeDialog.value = false
  removeErrors.value = {}
  selectedStudentId.value = null
  removeStep.value = 1
}

function openRemoveInstructorDialog() {
  selectedInstructorId.value = null
  removeInstructorErrors.value = {}
  removeInstructorStep.value = 1
  removeInstructorDialog.value = true
}

function cancelRemoveInstructor() {
  removeInstructorDialog.value = false
  removeInstructorErrors.value = {}
  selectedInstructorId.value = null
  removeInstructorStep.value = 1
}

function openDeleteDialog() {
  deleteStep.value = 1
  deleteDialog.value = true
}

function cancelDelete() {
  deleteDialog.value = false
  deleteStep.value = 1
}

function validateEdit(): boolean {
  editErrors.value = {}
  let valid = true

  if (!form.value.teamName.trim()) {
    editErrors.value.teamName = 'Team name is required.'
    valid = false
  }

  if (form.value.teamWebsiteUrl.trim()) {
    try {
      const url = new URL(form.value.teamWebsiteUrl.trim())
      if (url.protocol !== 'http:' && url.protocol !== 'https:') {
        editErrors.value.teamWebsiteUrl = 'Team website URL must start with http:// or https://.'
        valid = false
      }
    } catch {
      editErrors.value.teamWebsiteUrl = 'Team website URL must be a valid absolute URL.'
      valid = false
    }
  }

  return valid
}

function validateRemove(): boolean {
  removeErrors.value = {}
  if (!selectedStudentId.value) {
    removeErrors.value.studentId = 'Student is required.'
    return false
  }
  return true
}

function validateRemoveInstructor(): boolean {
  removeInstructorErrors.value = {}
  if (!selectedInstructorId.value) {
    removeInstructorErrors.value.instructorId = 'Instructor is required.'
    return false
  }
  return true
}

function goToEditPreview() {
  if (!validateEdit()) return
  editStep.value = 2
}

function goToRemovePreview() {
  if (!validateRemove()) return
  removeStep.value = 2
}

function goToRemoveInstructorPreview() {
  if (!validateRemoveInstructor()) return
  removeInstructorStep.value = 2
}

function goToDeletePreview() {
  deleteStep.value = 2
}

async function confirmEdit() {
  if (!team.value) return

  editSaving.value = true
  try {
    const res = await updateTeam(team.value.teamId, previewPayload.value) as any
    if (res.flag && res.data) {
      team.value = res.data
      editDialog.value = false
      snackbar.value = { show: true, message: 'Team updated successfully.', color: 'success' }
    }
  } catch (error: any) {
    const message = error?.response?.data?.message
    if (typeof message === 'string') {
      if (message.toLowerCase().includes('team name')) {
        editErrors.value.teamName = message
        editStep.value = 1
      } else if (message.toLowerCase().includes('website')) {
        editErrors.value.teamWebsiteUrl = message
        editStep.value = 1
      }
    }
  } finally {
    editSaving.value = false
  }
}

async function confirmRemove() {
  if (!team.value || !selectedTeamMember.value) return

  removeSaving.value = true
  const removedStudent = selectedTeamMember.value

  try {
    const res = await removeStudentFromTeam(team.value.teamId, removedStudent.studentId) as any
    if (res.flag && res.data) {
      const updatedTeam = res.data as TeamDetail
      team.value = updatedTeam
      removedStudentNotification.value = {
        fullName: removedStudent.fullName,
        email: removedStudent.email,
        teamName: updatedTeam.teamName,
        sectionName: updatedTeam.sectionName,
        status: 'Removed from team'
      }
      removeDialog.value = false
      removeErrors.value = {}
      selectedStudentId.value = null
      removeStep.value = 1
      snackbar.value = { show: true, message: 'Student removed from team successfully.', color: 'success' }
      return
    }

    snackbar.value = { show: true, message: res.message || 'Failed to remove student from team.', color: 'error' }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to remove student from team.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    removeSaving.value = false
  }
}

async function confirmRemoveInstructor() {
  if (!team.value || !selectedTeamInstructor.value) return

  removeInstructorSaving.value = true
  const removedInstructor = selectedTeamInstructor.value

  try {
    const res = await removeInstructorFromTeam(team.value.teamId, removedInstructor.instructorId) as any
    if (res.flag && res.data) {
      const updatedTeam = res.data as TeamDetail
      team.value = updatedTeam
      removedInstructorNotification.value = {
        fullName: removedInstructor.fullName,
        email: removedInstructor.email,
        teamName: updatedTeam.teamName,
        sectionName: updatedTeam.sectionName,
        status: 'Removed from team'
      }
      removeInstructorDialog.value = false
      removeInstructorErrors.value = {}
      selectedInstructorId.value = null
      removeInstructorStep.value = 1
      snackbar.value = { show: true, message: 'Instructor removed from team successfully.', color: 'success' }
      return
    }

    snackbar.value = { show: true, message: res.message || 'Failed to remove instructor from team.', color: 'error' }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to remove instructor from team.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    removeInstructorSaving.value = false
  }
}

async function confirmDelete() {
  if (!team.value) return

  deleteSaving.value = true
  const notification = buildDeletedTeamNotification(team.value)

  try {
    const res = await deleteTeam(team.value.teamId) as any
    if (res.flag) {
      teamNotificationsStore.setDeletedTeamNotification(notification)
      await router.push({ name: 'teams' })
      return
    }

    snackbar.value = { show: true, message: res.message || 'Failed to delete team.', color: 'error' }
  } catch (error: any) {
    const message = error?.response?.data?.message || 'Failed to delete team.'
    snackbar.value = { show: true, message, color: 'error' }
  } finally {
    deleteSaving.value = false
  }
}

function buildDeletedTeamNotification(currentTeam: TeamDetail): TeamDeletionNotification {
  return {
    teamId: currentTeam.teamId,
    teamName: currentTeam.teamName,
    sectionName: currentTeam.sectionName,
    studentNotifications: currentTeam.teamMembers.map((member) => ({
      fullName: member.fullName,
      email: member.email
    })),
    instructorNotifications: currentTeam.teamInstructors.map((instructor) => ({
      fullName: instructor.fullName,
      email: instructor.email
    })),
    status: 'Team deleted'
  }
}

function normalizeOptional(value: string): string | null {
  const trimmed = value.trim()
  return trimmed ? trimmed : null
}

async function openWarDialog() {
  if (!team.value) return
  warStep.value = 1
  warReport.value = null
  warSelectedWeek.value = ''
  activeWeeks.value = []
  warDialog.value = true

  try {
    const res = await getSection(team.value.sectionId) as any
    if (res.flag && res.data?.activeWeeks) {
      activeWeeks.value = [...res.data.activeWeeks].sort()
      if (activeWeeks.value.length) {
        const previousWeek = getPreviousIsoWeek()
        warSelectedWeek.value = activeWeeks.value.includes(previousWeek)
          ? previousWeek
          : activeWeeks.value[activeWeeks.value.length - 1] as string
      }
    }
  } catch {
    snackbar.value = { show: true, message: 'Failed to load section weeks.', color: 'error' }
  }
}

function getPreviousIsoWeek(): string {
  const d = new Date()
  d.setDate(d.getDate() - 7)
  const jan4 = new Date(d.getFullYear(), 0, 4)
  const dayOfYear = Math.floor((d.getTime() - new Date(d.getFullYear(), 0, 0).getTime()) / 86400000)
  const weekNum = Math.ceil((dayOfYear + jan4.getDay()) / 7)
  return `${d.getFullYear()}-W${String(weekNum).padStart(2, '0')}`
}

async function generateWarReport() {
  if (!team.value || !warSelectedWeek.value) return
  warLoading.value = true
  try {
    const res = await getTeamWarReport(team.value.teamId, warSelectedWeek.value) as any
    if (res.flag) {
      warReport.value = res.data
      warStep.value = 2
    } else {
      snackbar.value = { show: true, message: res.message || 'Failed to generate report.', color: 'error' }
    }
  } catch {
    snackbar.value = { show: true, message: 'An error occurred generating the report.', color: 'error' }
  } finally {
    warLoading.value = false
  }
}

function closeWarDialog() {
  warDialog.value = false
  warStep.value = 1
  warReport.value = null
}

async function openStudentWarDialog(member: { studentId: number; fullName: string }) {
  if (!team.value) return
  selectedStudent.value = member
  studentWarStep.value = 1
  studentWarReport.value = null
  studentWarStartWeek.value = ''
  studentWarEndWeek.value = ''
  studentWarDialog.value = true

  if (!activeWeeks.value.length) {
    try {
      const res = await getSection(team.value.sectionId) as any
      if (res.flag && res.data?.activeWeeks) {
        activeWeeks.value = [...res.data.activeWeeks].sort()
      }
    } catch {
      snackbar.value = { show: true, message: 'Failed to load section weeks.', color: 'error' }
    }
  }

  if (activeWeeks.value.length) {
    studentWarStartWeek.value = activeWeeks.value[0] as string
    studentWarEndWeek.value = activeWeeks.value[activeWeeks.value.length - 1] as string
  }
}

async function generateStudentWarReport() {
  if (!team.value || !selectedStudent.value || !studentWarStartWeek.value || !studentWarEndWeek.value) return
  studentWarLoading.value = true
  try {
    const res = await getStudentWarReport(
      team.value.teamId,
      selectedStudent.value.studentId,
      studentWarStartWeek.value,
      studentWarEndWeek.value
    ) as any
    if (res.flag) {
      studentWarReport.value = res.data
      studentWarStep.value = 2
    } else {
      snackbar.value = { show: true, message: res.message || 'Failed to generate report.', color: 'error' }
    }
  } catch {
    snackbar.value = { show: true, message: 'An error occurred generating the report.', color: 'error' }
  } finally {
    studentWarLoading.value = false
  }
}

function closeStudentWarDialog() {
  studentWarDialog.value = false
  studentWarStep.value = 1
  studentWarReport.value = null
  selectedStudent.value = null
}

async function openStudentPeerEvalDialog(member: { studentId: number; fullName: string }) {
  if (!team.value) return
  selectedStudentForPeerEval.value = member
  studentPeerEvalStep.value = 1
  studentPeerEvalReport.value = null
  studentPeerEvalStartWeek.value = ''
  studentPeerEvalEndWeek.value = ''
  studentPeerEvalDialog.value = true

  if (!activeWeeks.value.length) {
    try {
      const res = await getSection(team.value.sectionId) as any
      if (res.flag && res.data?.activeWeeks) {
        activeWeeks.value = [...res.data.activeWeeks].sort()
      }
    } catch {
      snackbar.value = { show: true, message: 'Failed to load section weeks.', color: 'error' }
    }
  }

  if (activeWeeks.value.length) {
    studentPeerEvalStartWeek.value = activeWeeks.value[0] as string
    studentPeerEvalEndWeek.value = activeWeeks.value[activeWeeks.value.length - 1] as string
  }
}

async function generateStudentPeerEvalReport() {
  if (!team.value || !selectedStudentForPeerEval.value || !studentPeerEvalStartWeek.value || !studentPeerEvalEndWeek.value) return
  studentPeerEvalLoading.value = true
  try {
    const res = await getInstructorStudentPeerEvalReport(
      team.value.teamId,
      selectedStudentForPeerEval.value.studentId,
      studentPeerEvalStartWeek.value,
      studentPeerEvalEndWeek.value
    ) as any
    if (res.flag) {
      studentPeerEvalReport.value = res.data
      studentPeerEvalStep.value = 2
    } else {
      snackbar.value = { show: true, message: res.message || 'Failed to generate report.', color: 'error' }
    }
  } catch {
    snackbar.value = { show: true, message: 'An error occurred generating the report.', color: 'error' }
  } finally {
    studentPeerEvalLoading.value = false
  }
}

function closeStudentPeerEvalDialog() {
  studentPeerEvalDialog.value = false
  studentPeerEvalStep.value = 1
  studentPeerEvalReport.value = null
  selectedStudentForPeerEval.value = null
}
</script>

<style scoped lang="scss">
.admin-actions {
  display: flex;
  gap: 12px;
}
</style>
