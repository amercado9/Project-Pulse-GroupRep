<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" md="8">
        <v-card rounded="lg" class="pa-4">
          <v-card-title class="d-flex align-center">
            <v-icon icon="mdi-account-circle-outline" class="mr-2" />
            My Profile
            <v-spacer />
            <v-btn
              v-if="!editing"
              color="primary"
              variant="text"
              prepend-icon="mdi-pencil"
              @click="startEditing"
            >
              Edit Details
            </v-btn>
          </v-card-title>

          <v-divider class="my-4" />

          <v-card-text>
            <v-form v-if="editing" ref="formRef" v-model="formValid" @submit.prevent="handleUpdate">
              <v-row>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editData.firstName"
                    label="First Name"
                    :rules="[v => !!v || 'First name is required']"
                    required
                  />
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editData.lastName"
                    label="Last Name"
                    :rules="[v => !!v || 'Last name is required']"
                    required
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="editData.email"
                    label="Email"
                    type="email"
                    :rules="[
                      v => !!v || 'Email is required',
                      v => /.+@.+\..+/.test(v) || 'Email must be valid'
                    ]"
                    required
                  />
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="editData.password"
                    label="New Password (leave blank to keep current)"
                    type="password"
                    hint="Only fill this if you want to change your password"
                    persistent-hint
                  />
                </v-col>
              </v-row>

              <div class="d-flex ga-2 mt-6">
                <v-btn
                  color="primary"
                  type="submit"
                  :loading="saving"
                  :disabled="!formValid"
                >
                  Save Changes
                </v-btn>
                <v-btn
                  variant="text"
                  @click="cancelEditing"
                  :disabled="saving"
                >
                  Cancel
                </v-btn>
              </div>
            </v-form>

            <div v-else class="py-4">
              <v-row>
                <v-col cols="12" sm="4" class="text-subtitle-2 text-medium-emphasis">Full Name</v-col>
                <v-col cols="12" sm="8" class="text-body-1">{{ userInfo?.firstName }} {{ userInfo?.lastName }}</v-col>

                <v-col cols="12" sm="4" class="text-subtitle-2 text-medium-emphasis">Email</v-col>
                <v-col cols="12" sm="8" class="text-body-1">{{ userInfo?.email }}</v-col>

                <v-col cols="12" sm="4" class="text-subtitle-2 text-medium-emphasis">Roles</v-col>
                <v-col cols="12" sm="8" class="text-body-1">
                  <v-chip
                    v-for="role in roleList"
                    :key="role"
                    size="small"
                    class="mr-1 text-capitalize"
                    variant="tonal"
                    color="primary"
                  >
                    {{ role }}
                  </v-chip>
                </v-col>
              </v-row>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- Confirmation Dialog -->
    <v-dialog v-model="confirmDialog" max-width="400">
      <v-card>
        <v-card-title>Confirm Changes</v-card-title>
        <v-card-text>
          Are you sure you want to save these changes to your account?
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="confirmDialog = false">Back</v-btn>
          <v-btn color="primary" @click="doUpdate">Confirm</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useUserInfoStore } from '@/stores/userInfo'
import { updateStudent } from '@/features/student/services/studentService'
import { useNotifyStore } from '@/stores/notify'
import type { StudentUpdateDto } from '@/features/student/services/studentTypes'

const userStore = useUserInfoStore()
const notifyStore = useNotifyStore()

const userInfo = computed(() => userStore.userInfo)
const roleList = computed(() => userStore.roleList)

const editing = ref(false)
const saving = ref(false)
const formValid = ref(false)
const confirmDialog = ref(false)
const formRef = ref<any>(null)

const editData = reactive<StudentUpdateDto>({
  firstName: '',
  lastName: '',
  email: '',
  password: ''
})

const startEditing = () => {
  if (userInfo.value) {
    editData.firstName = userInfo.value.firstName
    editData.lastName = userInfo.value.lastName
    editData.email = userInfo.value.email
    editData.password = ''
    editing.value = true
  }
}

const cancelEditing = () => {
  editing.value = false
}

const handleUpdate = async () => {
  const { valid } = await formRef.value.validate()
  if (valid) {
    confirmDialog.value = true
  }
}

const doUpdate = async () => {
  if (!userInfo.value) return

  confirmDialog.value = false
  saving.value = true

  try {
    const res = await updateStudent(userInfo.value.id, {
      firstName: editData.firstName,
      lastName: editData.lastName,
      email: editData.email,
      password: editData.password || undefined
    })

    if (res.data.flag) {
      notifyStore.success('Account updated successfully')
      // Update local store
      userStore.setUserInfo({
        ...userInfo.value,
        firstName: editData.firstName,
        lastName: editData.lastName,
        email: editData.email
      })
      editing.value = false
    } else {
      notifyStore.error(res.data.message || 'Failed to update account')
    }
  } catch (error: any) {
    notifyStore.error(error.response?.data?.message || 'An error occurred during update')
  } finally {
    saving.value = false
  }
}
</script>
