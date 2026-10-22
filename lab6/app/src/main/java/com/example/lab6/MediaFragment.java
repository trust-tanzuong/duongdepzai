package com.example.lab6;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab6.adapter.MediaAdapter;
import com.example.lab6.model.MediaModel;
import com.example.lab6.utils.SampleDataGenerator;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MediaFragment extends Fragment implements MediaAdapter.OnMediaClickListener {

    private MediaAdapter adapter;
    private TextView tvMediaTitle;
    private ImageButton btnDeleteMedia;
    private ProgressBar pbLoading;
    private LinearLayout llEmpty;

    private final List<MediaModel> allMediaList = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isSortNewestFirst = true;
    private int currentFilter = FILTER_ALL;

    private static final int FILTER_ALL = 0;
    private static final int FILTER_IMAGES = 1;
    private static final int FILTER_VIDEOS = 2;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), permissions -> {
                boolean granted = true;
                for (Boolean b : permissions.values()) {
                    if (Boolean.FALSE.equals(b)) {
                        granted = false;
                        break;
                    }
                }
                if (granted) {
                    loadMedia();
                } else {
                    Toast.makeText(getContext(), "Cần cấp quyền truy cập bộ nhớ/truyền thông!", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<IntentSenderRequest> deleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Đã xóa thành công!", Toast.LENGTH_SHORT).show();
                    loadMedia();
                } else {
                    Toast.makeText(getContext(), "Hủy thao tác xóa!", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvMedia = view.findViewById(R.id.rvMedia);
        tvMediaTitle = view.findViewById(R.id.tvMediaTitle);
        ImageButton btnSortMedia = view.findViewById(R.id.btnSortMedia);
        btnDeleteMedia = view.findViewById(R.id.btnDeleteMedia);
        ChipGroup cgFilterMedia = view.findViewById(R.id.cgFilterMedia);
        pbLoading = view.findViewById(R.id.pbLoadingMedia);
        llEmpty = view.findViewById(R.id.llEmptyMedia);
        Button btnCreateSampleMedia = view.findViewById(R.id.btnCreateSampleMedia);

        rvMedia.setLayoutManager(new GridLayoutManager(getContext(), 3));
        adapter = new MediaAdapter(this);
        rvMedia.setAdapter(adapter);

        btnSortMedia.setOnClickListener(v -> {
            isSortNewestFirst = !isSortNewestFirst;
            String msg = isSortNewestFirst ? "Sắp xếp: Mới nhất trước" : "Sắp xếp: Cũ nhất trước";
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
            filterAndDisplayMedia();
        });

        btnDeleteMedia.setOnClickListener(v -> deleteSelectedMedia());

        btnCreateSampleMedia.setOnClickListener(v -> {
            SampleDataGenerator.generateSampleMedia(requireContext());
            loadMedia();
        });

        cgFilterMedia.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipImages)) {
                currentFilter = FILTER_IMAGES;
            } else if (checkedIds.contains(R.id.chipVideos)) {
                currentFilter = FILTER_VIDEOS;
            } else {
                currentFilter = FILTER_ALL;
            }
            filterAndDisplayMedia();
        });

        checkPermissionsAndLoadMedia();
    }

    private void checkPermissionsAndLoadMedia() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                loadMedia();
            } else {
                requestPermissionsLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                });
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                loadMedia();
            } else {
                requestPermissionsLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                });
            }
        }
    }

    private void loadMedia() {
        pbLoading.setVisibility(View.VISIBLE);
        llEmpty.setVisibility(View.GONE);

        executor.execute(() -> {
            List<MediaModel> list = new ArrayList<>();
            if (getContext() == null) return;

            ContentResolver resolver = requireContext().getContentResolver();

            // 1. Query Images
            Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] imageProjection = new String[]{
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.DATA
            };

            try (Cursor cursor = resolver.query(imageUri, imageProjection, null, null, null)) {
                if (cursor != null) {
                    int idIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    int dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                    int sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idIdx);
                        String name = cursor.getString(nameIdx);
                        long dateAdded = cursor.getLong(dateIdx);
                        long size = cursor.getLong(sizeIdx);
                        String path = cursor.getString(dataIdx);
                        Uri uri = Uri.withAppendedPath(imageUri, String.valueOf(id));

                        list.add(new MediaModel(id, uri, name, path, dateAdded, false, 0, size));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 2. Query Videos
            Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            String[] videoProjection = new String[]{
                    MediaStore.Video.Media._ID,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.DATA
            };

            try (Cursor cursor = resolver.query(videoUri, videoProjection, null, null, null)) {
                if (cursor != null) {
                    int idIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                    int nameIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                    int dateIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                    int sizeIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                    int durationIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                    int dataIdx = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idIdx);
                        String name = cursor.getString(nameIdx);
                        long dateAdded = cursor.getLong(dateIdx);
                        long size = cursor.getLong(sizeIdx);
                        long duration = cursor.getLong(durationIdx);
                        String path = cursor.getString(dataIdx);
                        Uri uri = Uri.withAppendedPath(videoUri, String.valueOf(id));

                        list.add(new MediaModel(id, uri, name, path, dateAdded, true, duration, size));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    allMediaList.clear();
                    allMediaList.addAll(list);
                    filterAndDisplayMedia();
                    pbLoading.setVisibility(View.GONE);
                });
            }
        });
    }

    private void filterAndDisplayMedia() {
        List<MediaModel> filteredList = new ArrayList<>();

        for (MediaModel item : allMediaList) {
            if (currentFilter == FILTER_IMAGES && !item.isVideo()) {
                filteredList.add(item);
            } else if (currentFilter == FILTER_VIDEOS && item.isVideo()) {
                filteredList.add(item);
            } else if (currentFilter == FILTER_ALL) {
                filteredList.add(item);
            }
        }

        // Sort by DATE_ADDED
        Collections.sort(filteredList, (m1, m2) -> {
            if (isSortNewestFirst) {
                return Long.compare(m2.getDateAdded(), m1.getDateAdded());
            } else {
                return Long.compare(m1.getDateAdded(), m2.getDateAdded());
            }
        });

        adapter.setMediaList(filteredList);

        if (filteredList.isEmpty()) {
            llEmpty.setVisibility(View.VISIBLE);
        } else {
            llEmpty.setVisibility(View.GONE);
        }
    }

    private void deleteSelectedMedia() {
        List<MediaModel> selectedItems = adapter.getSelectedMedia();
        if (selectedItems.isEmpty()) {
            Toast.makeText(getContext(), "Chưa chọn hình/video nào!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Uri> urisToDelete = new ArrayList<>();
        for (MediaModel item : selectedItems) {
            if (item.getUri() != null) {
                urisToDelete.add(item.getUri());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                PendingIntent editPendingIntent = MediaStore.createDeleteRequest(requireContext().getContentResolver(), urisToDelete);
                IntentSenderRequest request = new IntentSenderRequest.Builder(editPendingIntent.getIntentSender()).build();
                deleteLauncher.launch(request);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Lỗi khi tạo yêu cầu xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            int deletedCount = 0;
            for (Uri uri : urisToDelete) {
                int count = requireContext().getContentResolver().delete(uri, null, null);
                if (count > 0) deletedCount++;
            }
            Toast.makeText(getContext(), "Đã xóa " + deletedCount + " tập tin!", Toast.LENGTH_SHORT).show();
            loadMedia();
        }
    }

    @Override
    public void onMediaClick(MediaModel media, int position) {
        Intent intent = new Intent(getContext(), MediaDetailActivity.class);
        intent.putExtra("media_item", media);
        startActivity(intent);
    }

    @Override
    public void onMediaLongClick(MediaModel media, int position) {
        // Selection mode toggled in adapter
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (selectedCount > 0) {
            tvMediaTitle.setText("Đã chọn: " + selectedCount);
            btnDeleteMedia.setVisibility(View.VISIBLE);
        } else {
            tvMediaTitle.setText("Thư Viện Media");
            btnDeleteMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}