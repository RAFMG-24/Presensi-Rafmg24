<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\ActivityLog;
use Illuminate\Http\Request;

class AuditLogController extends Controller
{
    public function index(Request $request)
    {
        $query = ActivityLog::orderBy('created_at', 'desc');

        if ($request->has('action')) {
            $query->where('action', $request->action);
        }
        if ($request->has('tanggal')) {
            $query->where('tanggal', $request->tanggal);
        }

        return response()->json([
            'success' => true,
            'data' => $query->paginate(30),
        ]);
    }
}
